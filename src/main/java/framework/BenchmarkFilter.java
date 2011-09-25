package framework;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

public class BenchmarkFilter implements Filter {

    public void init(FilterConfig filterConfig) throws ServletException {
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        System.out.printf("\n------------------------------ %15s ------------------------------\n", req.getServletPath());
        long started = System.currentTimeMillis();
        try {
            chain.doFilter(request, response);
        } finally {
            System.out.printf("------------------------------ %12d ms ------------------------------ \n", System.currentTimeMillis() - started);
        }
    }

    public void destroy() {
    }

}
