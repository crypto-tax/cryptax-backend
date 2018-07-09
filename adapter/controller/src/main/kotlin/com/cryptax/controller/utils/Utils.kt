package com.cryptax.controller.utils

import io.vertx.core.http.HttpServerResponse
import io.vertx.core.json.JsonObject

fun sendError(statusCode: Int, response: HttpServerResponse) {
	response
		.putHeader("content-type", "application/json")
		.setStatusCode(statusCode)
		.end()
}

fun sendSuccess(body: JsonObject, response: HttpServerResponse) {
	response
		.putHeader("content-type", "application/json")
		.end(body.encodePrettily())
}