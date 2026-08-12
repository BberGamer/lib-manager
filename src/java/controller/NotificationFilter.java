package controller;

import dao.NotificationDAO;
import model.User;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebFilter(urlPatterns = "/*")
public class NotificationFilter implements Filter {

    private final NotificationDAO notificationDAO = new NotificationDAO();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (request instanceof HttpServletRequest) {
            HttpServletRequest req = (HttpServletRequest) request;
            HttpSession session = req.getSession(false);
            if (session != null) {
                User user = (User) session.getAttribute("loggedUser");
                if (user != null) {
                    try {
                        int unreadCount = notificationDAO.getUnreadCount(user.getId());
                        req.setAttribute("headerUnreadCount", unreadCount);
                    } catch (Exception e) {
                        // ignore error to prevent blocking request
                    }
                }
            }
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {}
}
