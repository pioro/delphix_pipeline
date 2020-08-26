def call() {
    // Any valid steps can be called from this code, just like in other
    // Scripted Pipeline
    echo "Hello Engine"
    sh '/dxtoolkit/dx_get_appliance'
}
