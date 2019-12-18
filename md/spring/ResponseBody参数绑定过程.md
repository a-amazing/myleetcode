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