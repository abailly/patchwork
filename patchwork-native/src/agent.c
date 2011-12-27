#include <stdlib.h>
#include <stdio.h>
#include <memory.h>
#include "jvmti.h"


/************************************************************************/
/* handler for method entry event                                       */
/* This handler does (should do) the following: 
- check the method is within the filtered ones
- record the timestamp (should be synchronized across threads)     */
/************************************************************************/
static void JNICALL onMethodEntry(jvmtiEnv *jvmti_env,
								  JNIEnv* jni_env,
								  jthread thread,
								  jmethodID method)
{
	char *methodName,*methodSignature,*genericSignature;
	jvmtiError error;

	error = (*jvmti_env)->GetMethodName(jvmti_env, method,&methodName,&methodSignature,&genericSignature);
	if( JVMTI_ERROR_NONE != error)
	{
		fprintf(stdout,"error retrieving name of method\r\n");
		return;
	};

	fprintf(stdout,"entering %s (%s)\n\r",methodName,methodSignature);

	(*jvmti_env)->Deallocate(jvmti_env,methodName);
	(*jvmti_env)->Deallocate(jvmti_env,methodSignature);
	(*jvmti_env)->Deallocate(jvmti_env,genericSignature);
}

/**
* Callback from the VM that gets called upon loading when agent is started
* via JVM command-line
* see http://download.oracle.com/javase/6/docs/platform/jvmti/jvmti.html#onload
*/
JNIEXPORT jint JNICALL 
Agent_OnLoad(JavaVM *vm, char *options, void *reserved) 
{
	jvmtiCapabilities capabilities;
	jvmtiEnv *jvmti;
	jvmtiError error;
	jvmtiEventCallbacks callbacks;

	memset(&callbacks,0,sizeof(jvmtiEventCallbacks));
	memset(&capabilities,0,sizeof(jvmtiCapabilities));

	callbacks.MethodEntry = onMethodEntry;
	error = (*vm)->GetEnv(vm, &jvmti, JVMTI_VERSION_1_0);
	
	if(JNI_EVERSION == error) 
	{
		fprintf(stdout,"cannot create environment from JVM: %d\r\n", error);
		return -1;
	}

	error = (*jvmti)->SetEventCallbacks(jvmti,&callbacks,sizeof(jvmtiEventCallbacks));
	if( JVMTI_ERROR_NONE != error) 
	{
		fprintf(stdout,"error while setting events callback structure: %d\r\n", error);
		return -1;
	};

	capabilities.can_generate_method_entry_events = 1;
	error = (*jvmti)->AddCapabilities(jvmti,&capabilities);
	if( JVMTI_ERROR_NONE != error) 
	{
		fprintf(stdout,"error adding capabilities for method entry: %d\r\n", error);
		return -1;
	}

	error = (*jvmti)->SetEventNotificationMode(jvmti,JVMTI_ENABLE,JVMTI_EVENT_METHOD_ENTRY,NULL);
	if( JVMTI_ERROR_NONE != error )
	{
		fprintf(stdout,"error while enabling event METHOD_ENTRY %d \r\n", error);
		return -1;
	};

	fprintf(stdout, "start profiling\n\r");
	return 0;
}


/**
* Callback from the VM when shutting down.
*/
JNIEXPORT void JNICALL 
Agent_OnUnload(JavaVM *vm) 
{
	fprintf(stdout, "goodbye \n\r");	
}