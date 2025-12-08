package com.ownwn.datastore

import com.ownwn.server.Request
import com.ownwn.server.Response
import com.ownwn.server.intercept.Intercept
import com.ownwn.server.intercept.InterceptReciever
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

const val loginPath: String = "/login"
const val cookieName: String = "COOKIE_VALUE"

class Auth {
    @Intercept
    fun auth(request: Request, interceptor: InterceptReciever) {
        if (loginPath == request.path()) {
            return
        }

        if (request.cookies().isNullOrEmpty()) {
            interceptor.closeWithResponse(Response.softRedirect(loginPath))
            return
        }

        val cookieValue = DataStoreApplication.getEnv(cookieName) ?: run {
            System.err.println("Missing cookie value!")
            interceptor.closeWithResponse(Response.softRedirect(loginPath))
            return
        }


        val authenticated = request.cookies?.get(cookieName)?.let { URLDecoder.decode(it, StandardCharsets.UTF_8) } == cookieValue
        if (!authenticated) {
            interceptor.closeWithResponse(Response.softRedirect(loginPath))
            return
        }
    }
}

@Controller
class LoginPage {

    @RequestMapping(loginPath)
    fun welcome(): String {
        return "login"
    }
}