#include <jni.h>
#include <android/log.h>
#include "json.hpp"

//the json library comes from: https://github.com/nlohmann/json
// for convenience
using json = nlohmann::json;

extern "C"
jobject
Java_io_github_inesescin_nucleus_asyncTasks_MapMarkingAsyncTask_ndkParseJsonToNucleusArray(
        JNIEnv* env,
        jobject /* this */,
        jobject ecopointsMap,
        jstring stringResponse
) {
    jclass mapClass = env->FindClass("java/util/Map");
    jmethodID mapClass_putMethod = env->GetMethodID(mapClass,"put","(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");

    jclass nucleusClass = env->FindClass("io/github/inesescin/nucleus/models/Nucleus");
    jmethodID nucleusClass_init = env->GetMethodID(nucleusClass,"<init>","()V");
    jmethodID nucleusClass_setCoordinates = env->GetMethodID(nucleusClass,"setCoordinates","(Ljava/lang/String;)V");
    jmethodID nucleusClass_setValue = env->GetMethodID(nucleusClass,"setValue","(D)V");
    jmethodID nucleusClass_setStatus = env->GetMethodID(nucleusClass,"setStatus","(Ljava/lang/String;)V");

    const char * chars;
    jboolean isCopy=true;
    chars = env->GetStringUTFChars(stringResponse,&isCopy);
    json stringResponseJSON = json::parse(chars);
    json contextResponse = stringResponseJSON["contextResponses"];
    int len = contextResponse.size();
    for(int i = 0 ; i < len; i++){
        jobject nucleus = env->NewObject(nucleusClass,nucleusClass_init);
        json currentEntityResponse = contextResponse[i];
        json contextElement = currentEntityResponse["contextElement"];

        json attributes = contextElement["attributes"];
        std::string id = contextElement["id"];
        std::string coordinates = attributes[0]["value"];
        std::string value = attributes[1]["value"];
        std::string status = attributes[2]["value"];
        jdouble doubleValue = atof(value.c_str());

        env->CallVoidMethod(nucleus,nucleusClass_setCoordinates,env->NewStringUTF(coordinates.c_str()));
        env->CallVoidMethod(nucleus,nucleusClass_setValue,doubleValue);
        env->CallVoidMethod(nucleus,nucleusClass_setStatus,env->NewStringUTF(status.c_str()));
        env->CallObjectMethod(ecopointsMap,mapClass_putMethod,env->NewStringUTF(id.c_str()),nucleus);
        //__android_log_print(ANDROID_LOG_DEBUG,"LOG_TAG","JSON INFO:\nId: %s Coordinates: %s Value: %s Status: %s\n", id.c_str(),coordinates.c_str(),value.c_str(),status.c_str());
    }

    return ecopointsMap;
}