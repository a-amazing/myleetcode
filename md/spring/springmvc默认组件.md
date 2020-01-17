处理器映射器
HandlerMapping
- RequestMappingHandlerMapping
- BeanNameUrlHandlerMapping
- RouterFuntionMapping
- SimpleUrlHandlerMapping
- WelcomePageHandlerMapping

---

处理器适配器
HandlerAdapter

- RequestMappingHandlerAdapter
- HttpRequestHandlerAdapter
- SimpleContollerHandlerAdapter

```java
// 获取默认的 HandlerMethodArgumentResolver
private List<HandlerMethodArgumentResolver> getDefaultArgumentResolvers() { 
    List<HandlerMethodArgumentResolver> resolvers = new ArrayList<HandlerMethodArgumentResolver>();
    // 1.基于注解的参数解析 <-- 解析的数据来源主要是 HttpServletRequest | ModelAndViewContainer
    // Annotation-based argument resolution
    // 解析被注解 @RequestParam, @RequestPart 修饰的参数, 数据的获取通过 HttpServletRequest.getParameterValues
    resolvers.add(new RequestParamMethodArgumentResolver(getBeanFactory(), false));
    // 解析被注解 @RequestParam 修饰, 且类型是 Map 的参数, 数据的获取通过 HttpServletRequest.getParameterMap
    resolvers.add(new RequestParamMapMethodArgumentResolver());
    // 解析被注解 @PathVariable 修饰, 数据的获取通过 uriTemplateVars, 而 uriTemplateVars 却是通过 RequestMappingInfoHandlerMapping.handleMatch 生成, 其实就是 uri 中映射出的 key <-> value
    resolvers.add(new PathVariableMethodArgumentResolver());
    // 解析被注解 @PathVariable 修饰 且数据类型是 Map, 数据的获取通过 uriTemplateVars, 而 uriTemplateVars 却是通过 RequestMappingInfoHandlerMapping.handleMatch 生成, 其实就是 uri 中映射出的 key <-> value
    resolvers.add(new PathVariableMapMethodArgumentResolver());
    // 解析被注解 @MatrixVariable 修饰, 数据的获取通过 URI提取了;后存储的 uri template 变量值
    resolvers.add(new MatrixVariableMethodArgumentResolver());
    // 解析被注解 @MatrixVariable 修饰 且数据类型是 Map, 数据的获取通过 URI提取了;后存储的 uri template 变量值
    resolvers.add(new MatrixVariableMapMethodArgumentResolver());
    // 解析被注解 @ModelAttribute 修饰, 且类型是 Map 的参数, 数据的获取通过 ModelAndViewContainer 获取, 通过 DataBinder 进行绑定
    resolvers.add(new ServletModelAttributeMethodProcessor(false));
    // 解析被注解 @RequestBody 修饰的参数, 以及被@ResponseBody修饰的返回值, 数据的获取通过 HttpServletRequest 获取, 根据 MediaType通过HttpMessageConverter转换成对应的格式, 在处理返回值时 也是通过 MediaType 选择合适HttpMessageConverter, 进行转换格式, 并输出
    resolvers.add(new RequestResponseBodyMethodProcessor(getMessageConverters(), this.requestResponseBodyAdvice));
    // 解析被注解 @RequestPart 修饰, 数据的获取通过 HttpServletRequest.getParts()
    resolvers.add(new RequestPartMethodArgumentResolver(getMessageConverters(), this.requestResponseBodyAdvice));
    // 解析被注解 @RequestHeader 修饰, 数据的获取通过 HttpServletRequest.getHeaderValues()
    resolvers.add(new RequestHeaderMethodArgumentResolver(getBeanFactory()));
    // 解析被注解 @RequestHeader 修饰且参数类型是 Map, 数据的获取通过 HttpServletRequest.getHeaderValues()
    resolvers.add(new RequestHeaderMapMethodArgumentResolver());
    // 解析被注解 @CookieValue 修饰, 数据的获取通过 HttpServletRequest.getCookies()
    resolvers.add(new ServletCookieValueMethodArgumentResolver(getBeanFactory()));
    // 解析被注解 @Value 修饰, 数据在这里没有解析
    resolvers.add(new ExpressionValueMethodArgumentResolver(getBeanFactory()));
    // 解析被注解 @SessionAttribute 修饰, 数据的获取通过 HttpServletRequest.getAttribute(name, RequestAttributes.SCOPE_SESSION)
    resolvers.add(new SessionAttributeMethodArgumentResolver());
    // 解析被注解 @RequestAttribute 修饰, 数据的获取通过 HttpServletRequest.getAttribute(name, RequestAttributes.SCOPE_REQUEST)
    resolvers.add(new RequestAttributeMethodArgumentResolver());

    // 2.基于类型的参数解析器
    // Type-based argument resolution
    // 解析固定类型参数(比如: ServletRequest, HttpSession, InputStream 等), 参数的数据获取还是通过 HttpServletRequest
    resolvers.add(new ServletRequestMethodArgumentResolver());
    // 解析固定类型参数(比如: ServletResponse, OutputStream等), 参数的数据获取还是通过 HttpServletResponse
    resolvers.add(new ServletResponseMethodArgumentResolver());
    // 解析固定类型参数(比如: HttpEntity, RequestEntity 等), 参数的数据获取还是通过 HttpServletRequest
    resolvers.add(new HttpEntityMethodProcessor(getMessageConverters(), this.requestResponseBodyAdvice));
    // 解析固定类型参数(比如: RedirectAttributes), 参数的数据获取还是通过 HttpServletResponse
    resolvers.add(new RedirectAttributesMethodArgumentResolver());
    // 解析固定类型参数(比如: Model等), 参数的数据获取通过 ModelAndViewContainer
    resolvers.add(new ModelMethodProcessor());
    // 解析固定类型参数(比如: Model等), 参数的数据获取通过 ModelAndViewContainer
    resolvers.add(new MapMethodProcessor());
    // 解析固定类型参数(比如: Errors), 参数的数据获取通过 ModelAndViewContainer
    resolvers.add(new ErrorsMethodArgumentResolver());
    // 解析固定类型参数(比如: SessionStatus), 参数的数据获取通过 ModelAndViewContainer
    resolvers.add(new SessionStatusMethodArgumentResolver());
    // 解析固定类型参数(比如: UriComponentsBuilder), 参数的数据获取通过 HttpServletRequest
    resolvers.add(new UriComponentsBuilderMethodArgumentResolver());
    // 3.自定义参数解析器
    // Custom arguments
    if (getCustomArgumentResolvers() != null) {
        resolvers.addAll(getCustomArgumentResolvers());
    }
    // Catch-all
    //这两个解析器可以解析所有类型的参数
    resolvers.add(new RequestParamMethodArgumentResolver(getBeanFactory(), true));
    resolvers.add(new ServletModelAttributeMethodProcessor(true));
    return resolvers;
}
```



---

处理器异常解析器
HandlerExceptionResolver

- DefaultErrorAttributes
- HandlerExceptionResolverComposite
- ExceptionHandlerExceptionResolver
- ResponseStatusExceptionResolver
- DefaultHandlerExceptionResolver

---

对请求参数处理的拦截器链

RequestResponseBodyAdvice

根据``interface org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdvice`` 获取的可用拦截器如下:

- JsonViewRequestBodyAdvice 对应@JsonView注解