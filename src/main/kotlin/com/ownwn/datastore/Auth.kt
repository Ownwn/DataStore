package com.ownwn.datastore

import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.util.*

const val loginPath: String = "/login"
const val cookieName: String = "COOKIE_VALUE"

@Component
class Auth : Filter {
    override fun doFilter(req: ServletRequest?,res: ServletResponse?,chain: FilterChain) {
        if (req !is HttpServletRequest || res !is HttpServletResponse) {
            System.err.println("Bad req")
            return
        }

        if (loginPath == req.requestURI) {
            chain.doFilter(req, res)
            return
        }

        val cookies = req.cookies
        if (cookies == null || cookies.size == 0) {
            res.sendRedirect(loginPath)
            return
        }

        val cookieValue = DataStoreApplication.getEnv(cookieName) ?: run {
            System.err.println("Missing cookie value!")
            res.sendRedirect(loginPath)
            return
        }

        val authenticated = Arrays.stream(cookies).anyMatch { c: Cookie? -> cookieName == c?.name && cookieValue == URLDecoder.decode(c.value,
            StandardCharsets.UTF_8) }
        if (!authenticated) {
            res.sendRedirect(loginPath)
            return
        }

        chain.doFilter(req, res)
    }
}

@Controller
class LoginPage {

    @RequestMapping(loginPath)
    fun welcome(): String {
        return "login"
    }
}