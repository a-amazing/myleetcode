拦截器链 interceptorChain
adapter.invoke 开始调用

ServletInvocableHandlerMethod.invokeAndHandler -->
Object returnValue = invokeForRequest(webRequest, mavContainer, providedArgs); -->
Object[] args = getMethodArgumentValues(request, mavContainer, providedArgs); -->
//解析参数
for (int i = 0; i < parameters.length; i++) {
			MethodParameter parameter = parameters[i];
			parameter.initParameterNameDiscovery(this.parameterNameDiscoverer);
			args[i] = findProvidedArgument(parameter, providedArgs);
			if (args[i] != null) {
				continue;
			}
			if (!this.resolvers.supportsParameter(parameter)) {
				throw new IllegalStateException(formatArgumentError(parameter, "No suitable resolver"));
			}
			try {
                //真正调用参数解析器
				args[i] = this.resolvers.resolveArgument(parameter, mavContainer, request, this.dataBinderFactory);
			}
			catch (Exception ex) {
				// Leave stack trace for later, exception may actually be resolved and handled...
				if (logger.isDebugEnabled()) {
					String exMsg = ex.getMessage();
					if (exMsg != null && !exMsg.contains(parameter.getExecutable().toGenericString())) {
						logger.debug(formatArgumentError(parameter, exMsg));
					}
				}
				throw ex;
			}
		}

        //先获取支持该参数的解析器,根据
        HandlerMethodArgumentResolver resolver = getArgumentResolver(parameter);
        //先从缓存中获取
        HandlerMethodArgumentResolver result = this.argumentResolverCache.get(parameter);


        //获取当前请求对应的handler,通过HandlerMapping处理器映射器
        // Determine handler for the current request.
        mappedHandler = getHandler(processedRequest);
    
        //获取当前请求对应的处理器适配器,通过adapter.supports(handler),
        //return true即当前适配器支持当前请求,返回当前适配器
        // Determine handler adapter for the current request.
        HandlerAdapter ha = getHandlerAdapter(mappedHandler.getHandler());
    
        //开始真正调用处理器
        // Actually invoke the handler.
        mv = ha.handle(processedRequest, response, mappedHandler.getHandler());
        //真正调用适配器的handlerInternal方法
        adpter.handleInternal(request, response, (HandlerMethod) handler);
        以RequestMappingHandlerAdapter为例
        checkRequest(request);
---

RequestMappingHandlerMapping

RequestMappingHandlerAdapter --> AbstractHandlerMethodAdapter

```java
	@Override
	public final boolean supports(Object handler) {
        //supportsInternal((HandlerMethod)handler)) 默认为true
		return (handler instanceof HandlerMethod && supportsInternal((HandlerMethod) 		handler));
	}
```

---

之后开始调用拦截器链的preHandler()方法

```java
mappedHandler.applyPreHandle(processedRequest, response)
```

---

如果执行完所有拦截器后return true

```java
//通过adapter调用handler,返回一个ModelAndView对象
mv = ha.handle(processedRequest, response, mappedHandler.getHandler());
```

---

```java
//进去Adapter的handleInternal方法,具体实现由对应的Adapter实现,模板模式?
protected abstract ModelAndView handleInternal(HttpServletRequest request,
			HttpServletResponse response, HandlerMethod handlerMethod) throws Exception;
```

---

```java
//checkRequest,校验请求
//1.校验httpMethod是否支持
// Check whether we should support the request method.
String method = request.getMethod();
if (this.supportedMethods != null && !this.supportedMethods.contains(method)) {
    throw new HttpRequestMethodNotSupportedException(method, this.supportedMethods);
}

//2.是否需要session支持
// Check whether a session is required.
if (this.requireSession && request.getSession(false) == null) {
    throw new HttpSessionRequiredException("Pre-existing session required but none found");
}
```

---

继续向下执行,当不需要在session中同步执行时

```java
mav = invokeHandlerMethod(request, response, handlerMethod);
```

```java
//设置了参数解析器和返回值解析器的集合,在后面选取合适的解析器进行解析
if (this.argumentResolvers != null) {
				invocableMethod.setHandlerMethodArgumentResolvers(this.argumentResolvers);
			}
			if (this.returnValueHandlers != null) {
				invocableMethod.setHandlerMethodReturnValueHandlers(this.returnValueHandlers);
			}
```



```java
//非异步方法,执行controller方法
invocableMethod.invokeAndHandle(webRequest, mavContainer);
```



```java
//这个providedArgs	应该为null
Object returnValue = invokeForRequest(webRequest, mavContainer, providedArgs);
```



```java
//首先需要获取参数,逐渐接近了@RequestBody的参数解析绑定
Object[] args = getMethodArgumentValues(request, mavContainer, providedArgs);
```



```java
MethodParameter[] parameters = getMethodParameters();
if (ObjectUtils.isEmpty(parameters)) {
    return EMPTY_ARGS;
}

Object[] args = new Object[parameters.length];
for (int i = 0; i < parameters.length; i++) {
    MethodParameter parameter = parameters[i];
    parameter.initParameterNameDiscovery(this.parameterNameDiscoverer);
    //从providedArgs中获取参数,但传入的为null
    args[i] = findProvidedArgument(parameter, providedArgs);
    if (args[i] != null) {
        continue;
    }
    //继续向下执行
    if (!this.resolvers.supportsParameter(parameter)) {
        throw new IllegalStateException(formatArgumentError(parameter, "No suitable resolver"));
    }
    try {
        //通过resolver进行参数解析
        args[i] = this.resolvers.resolveArgument(parameter, mavContainer, request, this.dataBinderFactory);
    }
    catch (Exception ex) {
        // Leave stack trace for later, exception may actually be resolved and handled...
        if (logger.isDebugEnabled()) {
            String exMsg = ex.getMessage();
            if (exMsg != null && !exMsg.contains(parameter.getExecutable().toGenericString())) {
                logger.debug(formatArgumentError(parameter, exMsg));
            }
        }
        throw ex;
    }
}
return args;
```



```java
public Object resolveArgument(MethodParameter parameter, @Nullable ModelAndViewContainer mavContainer,
			NativeWebRequest webRequest, @Nullable WebDataBinderFactory binderFactory) throws Exception {
		//首先获取对应的参数解析器
		HandlerMethodArgumentResolver resolver = getArgumentResolver(parameter);
		if (resolver == null) {
			throw new IllegalArgumentException("Unsupported parameter type [" +
					parameter.getParameterType().getName() + "]. supportsParameter should be called first.");
		}
    	//然后进行解析,策略模式?Map策略
		return resolver.resolveArgument(parameter, mavContainer, webRequest, binderFactory);
	}
```



```java
	@Nullable
	private HandlerMethodArgumentResolver getArgumentResolver(MethodParameter parameter) {
        //获取参数解析器时,spring有缓存机制,可以快速找到对应的解析器
		HandlerMethodArgumentResolver result = this.argumentResolverCache.get(parameter);
		if (result == null) {
			for (HandlerMethodArgumentResolver resolver : this.argumentResolvers) {
                //resolver通过对应的supportsParameter方法判定是否支持对应参数
                /**
                RequestResponseBodyMethodProcess通过是否包含RequestBoody判定是否支持
                @Override
                public boolean supportsParameter(MethodParameter parameter) {
                    return parameter.hasParameterAnnotation(RequestBody.class);
                }
                */
				if (resolver.supportsParameter(parameter)) {
					result = resolver;
					this.argumentResolverCache.put(parameter, result);
					break;
				}
			}
		}
		return result;
	}
```



```java
	//进行参数解析的方法	
	@Override
	public Object resolveArgument(MethodParameter parameter, @Nullable ModelAndViewContainer mavContainer,
			NativeWebRequest webRequest, @Nullable WebDataBinderFactory binderFactory) throws Exception {

		parameter = parameter.nestedIfOptional();
		Object arg = readWithMessageConverters(webRequest, parameter, parameter.getNestedGenericParameterType());
		String name = Conventions.getVariableNameForParameter(parameter);

		if (binderFactory != null) {
			WebDataBinder binder = binderFactory.createBinder(webRequest, arg, name);
			if (arg != null) {
				validateIfApplicable(binder, parameter);
				if (binder.getBindingResult().hasErrors() && isBindExceptionRequired(binder, parameter)) {
					throw new MethodArgumentNotValidException(parameter, binder.getBindingResult());
				}
			}
			if (mavContainer != null) {
				mavContainer.addAttribute(BindingResult.MODEL_KEY_PREFIX + name, binder.getBindingResult());
			}
		}

		return adaptArgumentIfNecessary(arg, parameter);
	}
```



```java
//通过MessageConverter获取参数
Object arg = readWithMessageConverters(webRequest, parameter, parameter.getNestedGenericParameterType());
//-->
ServletServerHttpRequest inputMessage = new ServletServerHttpRequest(servletRequest);
Object arg = readWithMessageConverters(inputMessage, parameter, paramType);
```



```java
//先获取contentType,json格式的contentType为 application/json;charset=UTF-8
contentType = inputMessage.getHeaders().getContentType();
//DemoController.class
Class<?> contextClass = parameter.getContainingClass();
//UserReq.class
Class<T> targetClass = (targetType instanceof Class ? (Class<T>) targetType : null);
```

```java
private final RequestResponseBodyAdviceChain advice;
```

```java
//javaType即对参数Class的封装,有以下字段
/**
private static final long serialVersionUID = 1L;
    protected final Class<?> _class;
    protected final int _hash;
    protected final Object _valueHandler;
    protected final Object _typeHandler;
    protected final boolean _asStatic;
*/
return this.objectMapper.readValue(inputMessage.getBody(), javaType);
```

