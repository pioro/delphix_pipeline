"""
    This module defines utility functions such as reading configuration, setting up and terminating logging,
    exiting on error etc.

"""

from pathlib import Path
import argparse
import json
import datetime as dt
import sys
import csv

import app.log_manager.log_config as log_config
import app.utilities.encryption_manager as encryption_manager


class Utilities(encryption_manager.EncryptionManager):

    def __init__(self, local=False):

        self.config_file = Path(__file__).parent.parent.resolve().with_name('config') / 'app_config.json'
        self.stop_orchestrator = Path(__file__).parent.parent.resolve().with_name('config') / 'orchestrator.stop'
        self.log_dir = Path(__file__).parent.parent.resolve().with_name('logs')
        self.curr_raw_ts = dt.datetime.now()
        self.curr_ts = self.curr_raw_ts.strftime("%d%m%Y_%H%M%S")
        self.config_dict = dict()
        self.config_dict['session_raw_ts'] = self.curr_raw_ts
        self.config_dict['session_ts'] = self.curr_ts
        self.masking_logs = self.log_dir / 'masking_logs'
        self.postprocess_logs = self.log_dir / 'postprocess'
        self.file_format_storage = self.log_dir / 'file_formats'
        self.local_blob_storage = self.log_dir / 'local_blob_storage'


        if not local:
            parser = argparse.ArgumentParser()
            parser.add_argument('--debug', default=False, required=False, action='store_true')
            args = parser.parse_args()
            if args.debug:
                self.config_dict['debug_mode'] = True
            else:
                self.config_dict['debug_mode'] = False

        self.config_dict['postprocess_logs_dir'] = self.postprocess_logs
        self.config_dict['file_format_storage'] = self.file_format_storage
        self.config_dict['local_blob_storage'] = self.local_blob_storage
        self.config_dict['curr_ts'] = self.curr_ts
        self.config_dict['stop_orchestrator'] = self.stop_orchestrator

    def read_app_config(self):
        print(f'Reading config from : {self.config_file}')
        with open(self.config_file, encoding="utf-8") as f:
            application_properties = json.loads(f.read())

        if 'base_key' not in application_properties or not application_properties['base_key']:
            print('Base key is missing in config')
            self.exit_on_error()

        self.config_dict['base_key'] = application_properties['base_key']

        if 'secondary_key' not in application_properties or not application_properties['secondary_key']:
            print('Secondary key is missing in config')
            self.exit_on_error()

        self.config_dict['salt'] = application_properties['secondary_key']
        self.config_dict['jobs_per_engine'] = application_properties['max_jobs_per_engine']
        self.config_dict['num_engines'] = len(application_properties['masking_engines'])
        self.config_dict['input_queue'] = application_properties['input_queue']
        self.config_dict['postprocess_queue'] = application_properties['postprocess_queue']
        self.config_dict['source_env_name'] = application_properties['source_env_name']
        self.config_dict['target_env_name'] = application_properties['target_env_name']
        self.config_dict['domainname'] = application_properties['domainname']
        self.config_dict['execute_orchestrator'] = application_properties['execute_orchestrator']
        self.config_dict['execute_postprocessing'] = application_properties['execute_postprocessing']
        self.config_dict['filesystem'] = application_properties['filesystem']

        self.config_dict['cifs_source_mount'] = application_properties['cifs_source_mount']
        self.config_dict['cifs_target_mount'] = application_properties['cifs_target_mount']

        if (
                application_properties['execute_postprocessing'] or
                application_properties['send_messages_to_postprocessing']
        ):
            self.config_dict['send_to_postprocessing'] = True
        else:
            self.config_dict['send_to_postprocessing'] = False

        self.config_dict['engines'] = []
        master_available = False
        for each_engine in application_properties['masking_engines']:
            engine_details = dict()
            if 'name' in each_engine:
                engine_details['name'] = each_engine['name']
            if 'max_parallel_jobs' in each_engine:
                engine_details['max_parallel_jobs'] = each_engine['max_parallel_jobs']
            if 'access_url' not in each_engine or not each_engine['access_url']:
                print(f'Access url is missing for at least one of the engines in the config')
                self.exit_on_error()
            engine_details['access_url'] = each_engine['access_url']
            if 'is_master' in each_engine:
                engine_details['is_master'] = each_engine['is_master']
                if each_engine['is_master']:
                    master_available = True
            if 'user' not in each_engine or not each_engine['user']:
                print(f'Login user missing for at least one of the engines in the config')
                self.exit_on_error()

            try:
                user = self.decrypt(
                    self.config_dict['base_key'], self.config_dict['salt'], 'compliance_engine_user',
                each_engine['user'])
                engine_details['user'] = user
            except Exception as excp:
                print('Failed to decrypt the user for at least one of the masking engines')
                self.exit_on_error()

            if 'password' not in each_engine or not each_engine['password']:
                print(f'Password missing for at least one of the engines in the config')
                self.exit_on_error()

            try:
                password = self.decrypt(
                    self.config_dict['base_key'], self.config_dict['salt'], 'compliance-engine-password',
                    each_engine['password'])
                engine_details['password'] = password
            except Exception as excp:
                print('Failed to decrypt the password for at least one of the masking engines')
                self.exit_on_error()

            self.config_dict['engines'].append(engine_details)

        if not master_available:
            print('No engine is defined as master in the config. At least 1 engine needs to be master')
            self.exit_on_error()

    def get_logger(self):
        #log_file = self.log_dir / f'app_log_{self.curr_ts}.log'
        log_file = self.log_dir / f'app_log.log'
        if self.config_dict['debug_mode']:
            log_object = log_config.LogConfig(log_file, debug=True)
        else:
            log_object = log_config.LogConfig(log_file, debug=False)
        app_logger = log_object.get_module_logger()
        print(f'Check logs in : {log_file}')
        return log_object, app_logger

    def setup_mp_logging(self, proc_num, log_ts, debug_mode, log_type=None):
        #TODO
        # fix logging
        if log_type == 'postprocess':
            log_file = self.postprocess_logs / f'postprocess.log'
        elif log_type == 'masking':
            log_file = self.masking_logs / f'engine{proc_num}.log'
        else:
            log_file = self.log_dir / f'proc{proc_num}.log'

        if debug_mode:
            proc_log_object = log_config.LogConfig(log_file, debug=True)
        else:
            proc_log_object = log_config.LogConfig(log_file, debug=False)

        return proc_log_object

    def terminate_logging(self, log_object, print_out=True):
        if print_out:
            print('Terminating logging')
        log_object.close_handler()

    def exit_on_error(self, log_object=None):
        if log_object:
            print('Operation failed, check logs')
            self.terminate_logging(log_object)
        else:
            print('Operation failed')
        sys.exit(1)
