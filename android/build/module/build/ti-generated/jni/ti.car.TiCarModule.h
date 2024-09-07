/**
 * Titanium SDK
 * Copyright TiDev, Inc. 04/07/2022-Present
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */

/** This is generated, do not edit by hand. **/

#include "Proxy.h"

namespace ti {
namespace car {

class TiCarModule : public titanium::Proxy
{
public:
	explicit TiCarModule();

	static void bindProxy(v8::Local<v8::Object>, v8::Local<v8::Context>);
	static v8::Local<v8::FunctionTemplate> getProxyTemplate(v8::Isolate*);
	static v8::Local<v8::FunctionTemplate> getProxyTemplate(v8::Local<v8::Context>);
	static void dispose(v8::Isolate*);

	static jclass javaClass;

private:
	static v8::Persistent<v8::FunctionTemplate> proxyTemplate;
	static v8::Persistent<v8::Object> moduleInstance;

	// Methods -----------------------------------------------------------
	static void createGridTemplate(const v8::FunctionCallbackInfo<v8::Value>&);
	static void createTabTemplate(const v8::FunctionCallbackInfo<v8::Value>&);
	static void createListTemplate(const v8::FunctionCallbackInfo<v8::Value>&);
	static void createMessageTemplate(const v8::FunctionCallbackInfo<v8::Value>&);
	static void createToast(const v8::FunctionCallbackInfo<v8::Value>&);

	// Dynamic property accessors ----------------------------------------

};

} // car
} // ti
