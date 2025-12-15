package com.ownwn.server.intercept;

import com.ownwn.server.response.Response;

public class InterceptReceiver {
    private Response response;

    /** calling this method will cause the http request to be closed, and it will not reach the destination Request */
    public void closeWithResponse(Response response) {
        if (this.response != null) {
            throw new RuntimeException("Response already closed!");
        }
        this.response = response;
    }

    public boolean isClosed() {
        return response != null;
    }

    public Response getResponse() {
        return response;
    }
}
