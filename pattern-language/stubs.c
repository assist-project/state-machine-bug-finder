#include <jni.h>
#include <caml/mlvalues.h>
#include <caml/memory.h>
#include <caml/alloc.h>
#include <caml/custom.h>
#include <caml/callback.h>


// Global flag for OCaml runtime initialization
static int ocaml_initialized = 0;

// Function to initialize the OCaml runtime (only once)
void ensure_ocaml_initialized() {
    if (!ocaml_initialized) {
        char *caml_argv[1] = { NULL };
        caml_startup(caml_argv);
        ocaml_initialized = 1;
    }
}

// Converts a java string to a C String
const char* convertJavaString(JNIEnv *env, jstring str) {
    const char* cstr = (*env)->GetStringUTFChars(env, str, NULL);
    if (cstr == NULL) {
        fprintf(stderr, "Could not allocate memory during conversion of jstring to C string\n");
        exit(-1);
    }
    return cstr;
}

const value* getOcamlFunction(char* name) {
    const value* ocaml_func = caml_named_value(name);
    if (ocaml_func == NULL) {
        fprintf(stderr, "Could not find OCaml function \"%s\"\n", name);
        exit(-1);
    }
    return ocaml_func;
}

value ocaml_function_res(char* name, JNIEnv *env, jstring path) {
    const value* ocaml_func = getOcamlFunction(name);
    const char* cstr = convertJavaString(env, path);
    
    value ocaml_string = caml_copy_string(cstr);
    value rv = caml_callback(*ocaml_func, ocaml_string);
    (*env)->ReleaseStringUTFChars(env, path, cstr);

    return rv;
}

JNIEXPORT jobject JNICALL Java_se_uu_it_smbugfinder_encoding_OcamlValues_getFieldsOcaml(JNIEnv *env, jobject obj, jstring path) {
    ensure_ocaml_initialized();

    value rv = ocaml_function_res("ocaml_get_fields", env, path);

    jclass hashMapClass = (*env)->FindClass(env, "java/util/HashMap");
    jmethodID hashMapConstructor = (*env)->GetMethodID(env, hashMapClass, "<init>", "()V");

    // Assuming ocaml_result is a list of pairs (key, string_list)
    jobject hashMap = (*env)->NewObject(env, hashMapClass, hashMapConstructor);

    while (Is_block(rv)) {
        value pair = Field(rv, 0);  // Head of the list (pair)
        value key = Field(pair, 0);           // Key (string)
        value val_list = Field(pair, 1);      // Value (list of strings)

        jstring jkey = (*env)->NewStringUTF(env, String_val(key));

        jclass arrayListClass = (*env)->FindClass(env, "java/util/ArrayList");
        jmethodID arrayListConstructor = (*env)->GetMethodID(env, arrayListClass, "<init>", "()V");
        jobject arrayList = (*env)->NewObject(env, arrayListClass, arrayListConstructor);

        // keep add method in a variable
        jmethodID arrayListAdd = (*env)->GetMethodID(env, arrayListClass, "add", "(Ljava/lang/Object;)Z");

        while (Is_block(val_list)) {
            value item = Field(val_list, 0); //head of value list
            jstring jstr = (*env)->NewStringUTF(env, String_val(item));
            (*env)->CallBooleanMethod(env, arrayList, arrayListAdd, jstr);
            val_list = Field(val_list, 1);  // Move to next in the list
        }

        jmethodID hashMapPut = (*env)->GetMethodID(env, hashMapClass, "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
        (*env)->CallObjectMethod(env, hashMap, hashMapPut, jkey, arrayList);

        rv = Field(rv, 1);  // Move to next pair in the list
    }

    return hashMap;
}


JNIEXPORT jobject JNICALL Java_se_uu_it_smbugfinder_encoding_OcamlValues_getMessageMapOcaml(JNIEnv *env, jobject obj, jstring path) {
    ensure_ocaml_initialized();

    value rv = ocaml_function_res("ocaml_get_messages_map", env, path);
    
    // store the HashMap class, its constructor and functions that will be used later
    jclass hashMapClass = (*env)->FindClass(env, "java/util/HashMap");
    jmethodID hashMapConstructor = (*env)->GetMethodID(env, hashMapClass, "<init>", "()V");
    jmethodID hashMapPut = (*env)->GetMethodID(env, hashMapClass, "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
    
    // same for ArrayList class
    jclass arrayListClass = (*env)->FindClass(env, "java/util/ArrayList");
    jmethodID arrayListConstructor = (*env)->GetMethodID(env, arrayListClass, "<init>", "()V");
    jmethodID arrayListAdd = (*env)->GetMethodID(env, arrayListClass, "add", "(Ljava/lang/Object;)Z"); //try to return void (V instead of Z)
    
    // create a java hashmap object 
    jobject hashMap = (*env)->NewObject(env, hashMapClass, hashMapConstructor);
    
    while (Is_block(rv)) {
        value pair = Field(rv, 0);       // get head of list
        value mes = Field(pair, 0);      // get message
        value ty = Field(pair, 1);       // get field type
        value field = Field(pair, 2);    // get field
    
        // create java strings from the ocaml ones
        jstring jkey = (*env)->NewStringUTF(env, String_val(mes));
        jstring jty = (*env)->NewStringUTF(env, String_val(ty));
        jstring jfield = (*env)->NewStringUTF(env, String_val(field));

        // add the opt and field in an array
        jobject arrayList = (*env)->NewObject(env, arrayListClass, arrayListConstructor);
        (*env)->CallBooleanMethod(env, arrayList, arrayListAdd, jty);
        (*env)->CallBooleanMethod(env, arrayList, arrayListAdd, jfield);

        (*env)->CallObjectMethod(env, hashMap, hashMapPut, jkey, arrayList);
        rv = Field(rv, 1);              // get next pair from the list
    }

    return hashMap;
}

JNIEXPORT jobject JNICALL Java_se_uu_it_smbugfinder_encoding_OcamlValues_getFunctionsOcaml(JNIEnv *env, jobject obj, jstring path) {
    ensure_ocaml_initialized();

    value rv = ocaml_function_res("ocaml_get_functions", env, path);

    // store the ArrayList class, its constructor and functions that will be used later
    jclass arrayListClass = (*env)->FindClass(env, "java/util/ArrayList");
    jmethodID arrayListConstructor = (*env)->GetMethodID(env, arrayListClass, "<init>", "()V");
    jmethodID arrayListAdd = (*env)->GetMethodID(env, arrayListClass, "add", "(Ljava/lang/Object;)Z");

    // store the HashMap class, its constructor and functions that will be used later
    jclass hashMapClass = (*env)->FindClass(env, "java/util/HashMap");
    jmethodID hashMapConstructor = (*env)->GetMethodID(env, hashMapClass, "<init>", "()V");
    jmethodID hashMapPut = (*env)->GetMethodID(env, hashMapClass, "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");

    // create a java ArrayList object
    jobject mainList = (*env)->NewObject(env, arrayListClass, arrayListConstructor);

    while (Is_block(rv)) {
        value func = Field(rv, 0);        // get head of list
        value name = Field(func, 0);      // get function name
        value mapList  = Field(func, 1);      // get function's map (a list of pairs)

        // create java string from ocaml string
        jstring jname = (*env)->NewStringUTF(env, String_val(name));
        
        jobject hashMap = (*env)->NewObject(env, hashMapClass, hashMapConstructor);
        while (Is_block(mapList)) {
            value pair = Field(mapList, 0);  // get head of list
            value key = Field(pair, 0);      // get key
            value val = Field(pair, 1);      // get value

            // create java strings from the ocaml ones
            jstring jkey = (*env)->NewStringUTF(env, String_val(key));
            jstring jval = (*env)->NewStringUTF(env, String_val(val));

            (*env)->CallObjectMethod(env, hashMap, hashMapPut, jkey, jval);
            mapList = Field(mapList, 1);
        } // now hashMap has all the key-value pairs

        jobject functionList = (*env)->NewObject(env, arrayListClass, arrayListConstructor);
        (*env)->CallBooleanMethod(env, functionList, arrayListAdd, jname);
        (*env)->CallBooleanMethod(env, functionList, arrayListAdd, hashMap);
        
        (*env)->CallBooleanMethod(env, mainList, arrayListAdd, functionList);

        rv = Field(rv, 1);
    }

    return mainList;
}
