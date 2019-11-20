```java
	@Override
	protected void doFilterInternal(HttpServletRequest request,
			HttpServletResponse response, FilterChain filterChain)
					throws ServletException, IOException {
		request.setAttribute(HttpServletResponse.class.getName(), response);
		//从请求中获取token信息
        //CookieCsrfTokenRepository-->token信息存储在cookie中
        //HttpSessionCsrfTokenRepository-->token信息存储在session中
        //以下以cookie中为例
        //获取名为XSRF-TOKEN的cookie的值
        //DefaultCsrfToken(String headerName, String parameterName, String token)
        //new DefaultCsrfToken("X-XSRF-TOKEN","_csrf",cookie值);
		CsrfToken csrfToken = this.tokenRepository.loadToken(request);
		final boolean missingToken = csrfToken == null;
		if (missingToken) {
			csrfToken = this.tokenRepository.generateToken(request);
			this.tokenRepository.saveToken(csrfToken, request, response);
		}
		request.setAttribute(CsrfToken.class.getName(), csrfToken);
		request.setAttribute(csrfToken.getParameterName(), csrfToken);
		
        //DefaultRequiresCsrfMatcher
        //只匹配http method包括"GET", "HEAD", "TRACE", "OPTIONS"
        //@Override
		/**public boolean matches(HttpServletRequest request) {
			为什么这边的结果要取反?
			return !this.allowedMethods.contains(request.getMethod());
		}*/
        //负负得正,即包含该http method
        //为什么这些请求不需要过滤呢,因为GET等请求按照restful规范,是一些查询类的请求,不会对
        //后台数据造成影响,所以默认不过滤
		if (!this.requireCsrfProtectionMatcher.matches(request)) {
			filterChain.doFilter(request, response);
			return;
		}
		//取真实token,request.getHeader("X-XSRF-TOKEN");
		String actualToken = request.getHeader(csrfToken.getHeaderName());
		if (actualToken == null) {
            //尝试request.getHeader("_csrf");
			actualToken = request.getParameter(csrfToken.getParameterName());
		}
        //两个token不相同!
		if (!csrfToken.getToken().equals(actualToken)) {
			if (this.logger.isDebugEnabled()) {
				this.logger.debug("Invalid CSRF token found for "
						+ UrlUtils.buildFullRequestUrl(request));
			}
			if (missingToken) {
				this.accessDeniedHandler.handle(request, response,
						new MissingCsrfTokenException(actualToken));
			}	
			else {
				this.accessDeniedHandler.handle(request, response,
						new InvalidCsrfTokenException(csrfToken, actualToken));
			}
			return;
		}

		filterChain.doFilter(request, response);
	}
```

查看CsrfFilter的doFilterInternal()方法后,发现spring用来防止跨域攻击的方法是在请求的header中带上一个特点headerName 的请求头,判断cookie中的token与header中的token是否一致,来确认这个请求是否是由指定网页发起的,而非跨域攻击