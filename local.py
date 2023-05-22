import json
import app.orchestration_modules.preprocessing as preprocessing
import app.orchestration_modules.ddf as ddf
import app.utilities.utils as utils
import app.utilities.config_helpers as config_helpers
import app.api_modules.masking_api_resources.login as login
import os
import logging
from app.azure_support.message_types import QueueMessage
import argparse



def login_to_engine(config_dict, engine_id):
    logger = logging.getLogger()
    thread_number = 1
    base_url, user, password = config_helpers.get_engine(config_dict, engine_id)
    if base_url:
        login_obj = login.Login()
        status, auth = login_obj.login(base_url, user, password)
        if not status:
            logger.error(f"Thread {thread_number} : Can't loging to engine {base_url}")
            logger.error(f"Aborting thread : {thread_number} for engine : {engine_id}")
            return
        return auth["Authorization"] 
                      
    else:
        logger.error(f"Thread {thread_number} : Can't find engine configuration {engine_id}")
        logger.error(f"Aborting thread : {thread_number} for engine : {engine_id}")
        return


def define_masking(config_dict, engine_id, ddf_filename, msg: QueueMessage):

    logger = logging.getLogger()

    auth_key = login_to_engine(config_dict, engine_id)

    if auth_key is None:
        logger.error("Can't login to engine")
        return

    preprocess_obj = preprocessing.PreProcessing(config_dict, engine_id, auth_key)

    preprocess_obj.filesystem = 'NFS'

    # Reading DDF

    try:
        with open(ddf_filename, "rt") as fh:
            ddffile = fh.read()
        ddf_obj = ddf.DDF(ddffile)
    except json.JSONDecodeError as e:
        print(f"Can't parse DDF file. Error: {e}")
        logger.error(f"Can't parse DDF file. Error: {e}")
        return 


    logger.debug('Successfully read DDF')

    # # Read checksum file and validate data files listed in checksum exist on source directory
    ret_status, error_text = preprocess_obj.data_file_list_match(msg)
    if not ret_status:
        logger.error(f'Error in validating the existence of data files listed in checksum file '
                            f'for : {msg.appname_with_table}')
        logger.error(f'Skipping table')
        return False, None, None, None, error_text

    logger.debug(f'Successfully validated existence of data files mentioned in checksum file')

    # # Validate that header of data files matches DDF

    files_to_mask = preprocess_obj.read_checksum_file(msg)

    for filename in files_to_mask:
        # Validate header matches for the first data file and DDF
        # It is assumed that if the first file matches, other data files will match
        ret_status, error_text = preprocess_obj.validate_header_with_ddf(msg, ddf_obj, filename)
        if not ret_status:
            return False, None, None, None, error_text
        break

    logger.debug('Successfully validated that header of data files matches the description in DDF')

    # # number of streams per job is calculated using the following rules
    # # 10 files to use 1 stream
    # # max number of streams per job can't be higher than number of jobs_per_engine

    streams_no = min( 7 , round(len(files_to_mask) / 10))  
    
    ret_status, source_connector_id, error_text = preprocess_obj.create_source_connector(msg, ddf_obj)
    if not ret_status:
        logger.error('Failed to create source connector')
        logger.error('Skipping table')
        return False, None, None, None, error_text

    logger.debug(f'Successfully created source connector with ID : {source_connector_id}')


    ret_status, error_text = preprocess_obj.test_connector(source_connector_id)
    if not ret_status:
        logger.error(f"Test for connector {source_connector_id} failed with response {error_text}")
        logger.error('Skipping table')
        return False, None, None, None, error_text

    logger.info(f"Test for connector with connector_id {source_connector_id} is succesful")

    ret_status, target_connector_id, error_text = preprocess_obj.create_target_connector(msg, ddf_obj)
    if not ret_status:
        logger.error('Failed to create target connector')
        logger.error('Skipping table')
        return False, None, None, None, error_text
    
    logger.debug(f'Successfully created target connector with ID : {target_connector_id}')

    ret_status, error_text = preprocess_obj.test_connector(target_connector_id)
    if not ret_status:
        logger.error(f"Test for connector {target_connector_id} failed with response {error_text}")
        logger.error('Skipping table')
        return False, None, None, None, error_text

    logger.info(f"Test for target connector with connector_id {target_connector_id} is succesful")

    ret_status, ruleset_id, error_text = preprocess_obj.create_ruleset(msg, source_connector_id)
    if not ret_status:
        logger.error('Failed to create ruleset')
        logger.error('Skipping table')
        return False, None, None, None, error_text

    logger.debug(f'Successfully created ruleset wih ID : {ruleset_id}')

    ret_status, file_format_id, error_text = preprocess_obj.create_fileformat(msg, ddf_obj)
    if not ret_status:
        logger.error('Failed to create file format')
        logger.error('Skipping table')
        return False, None, None, None, error_text

    logger.debug(f'Successfully created file format with ID : {file_format_id}')

    ret_status, error_text = preprocess_obj.create_inventory(msg, ddf_obj, ruleset_id, file_format_id,
                                                            source_connector_id)
    if not ret_status:
        logger.error('Failed to configure inventory for masking')
        logger.error('Skipping table')
        return False, None, None, None, error_text

    logger.debug(f'Successfully configured inventory for ruleset with ID : {ruleset_id} '
                        f'and file format with ID : {file_format_id}')

    ret_status, job_id, error_text = preprocess_obj.create_masking_job(msg, target_connector_id, ruleset_id, streams_no)
    if not ret_status:
        logger.error('Failed to create masking job')
        logger.error('Skipping table')
        return False, None, None, None, error_text

    logger.info(f"Successfully created masking job with ID : {job_id}")
    return True, job_id, source_connector_id, target_connector_id, None



def set_logging():
    handler = logging.FileHandler(f'manual_run.log')
    logger = logging.getLogger()
    log_date_format = "%y-%m-%d %H:%M:%S"
    log_format = "%(asctime)s %(name)-20s  %(threadName)-10s %(module)-21s: %(lineno)4d [%(levelname)-5s] %(message)s"
    formatter = logging.Formatter(log_format, log_date_format)
    logger.setLevel(logging.DEBUG)
    handler.setFormatter(formatter)
    logger.addHandler(handler)
    logger.debug(f"HELLO FROM MOD1 {os.getpid()}")



if __name__ == "__main__":
    print('Starting Application')

    utils_obj = utils.Utilities(local=True)
    utils_obj.read_app_config()

    parser = argparse.ArgumentParser()
    parser.add_argument('--ddffile', default=False, required=True)
    parser.add_argument('--app_name', default=False, required=True)
    parser.add_argument('--storage_account', default=False, required=True)
    parser.add_argument('--folder', default=False, required=True)
    parser.add_argument('--file_pattern', default=False, required=True)
    args = parser.parse_args()


    test_engine_id = 0
    test_thread_id = 1

    utils_obj.config_dict['debug_mode'] = True

    set_logging()

    # app_name = '9052666fisty'
    # storage_account = 'deveronpd01usectostg'
    # folder = 'stg/9052666fisty'
    # file_pattern = "20230410-9052666fisty-ntsc01-0001-plt1-cus1-m"
    # ddfile = '9052666fisty-ntsc01.ddf'

    ddffile = args.ddffile
    app_name = args.app_name
    storage_account = args.storage_account
    folder = args.folder
    file_pattern = args.file_pattern



    msg = QueueMessage(
        application_name=app_name,
        source_storage_account=storage_account,
        app_folder_in_container=folder,
        file_pattern=file_pattern
    )

    define_masking(utils_obj.config_dict, test_engine_id, ddffile,  msg)