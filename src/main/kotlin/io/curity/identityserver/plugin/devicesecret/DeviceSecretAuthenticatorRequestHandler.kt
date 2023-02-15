/*
 * Copyright 2022 Curity AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.curity.identityserver.plugin.devicesecret

import io.curity.identityserver.plugin.devicesecret.GetRepresentationFunction.Companion.DEVICE_SECRET_FIELD_NAME
import io.curity.identityserver.plugin.devicesecret.GetRepresentationFunction.Companion.TEMPLATE_PATH
import se.curity.identityserver.sdk.attribute.Attribute
import se.curity.identityserver.sdk.attribute.AuthenticationAttributes
import se.curity.identityserver.sdk.attribute.ContextAttributes
import se.curity.identityserver.sdk.attribute.MapAttributeValue
import se.curity.identityserver.sdk.attribute.SubjectAttributes
import se.curity.identityserver.sdk.authentication.AuthenticationResult
import se.curity.identityserver.sdk.authentication.AuthenticatorRequestHandler
import se.curity.identityserver.sdk.errors.ErrorCode
import se.curity.identityserver.sdk.haapi.HaapiContract
import se.curity.identityserver.sdk.haapi.Message
import se.curity.identityserver.sdk.haapi.RepresentationFactory
import se.curity.identityserver.sdk.haapi.RepresentationFunction
import se.curity.identityserver.sdk.haapi.RepresentationModel
import se.curity.identityserver.sdk.http.HttpMethod
import se.curity.identityserver.sdk.http.MediaType
import se.curity.identityserver.sdk.service.ExceptionFactory
import se.curity.identityserver.sdk.web.Representation
import se.curity.identityserver.sdk.web.Request
import se.curity.identityserver.sdk.web.Response
import se.curity.identityserver.sdk.web.ResponseModel
import java.net.URI
import java.util.Optional

class DeviceSecretAuthenticatorRequestHandler(
    private val config: DeviceSecretAuthenticatorPluginConfig,
    private val exceptionFactory: ExceptionFactory
) : AuthenticatorRequestHandler<DeviceSecretModel> {

    override fun preProcess(request: Request, response: Response): DeviceSecretModel {
        response.setResponseModel(
            ResponseModel.templateResponseModel(
                emptyMap(), TEMPLATE_PATH
            ), Response.ResponseModelScope.ANY
        )

        return DeviceSecretModel(request)
    }

    override fun get(model: DeviceSecretModel, response: Response): Optional<AuthenticationResult> = Optional.empty()

    override fun post(model: DeviceSecretModel, response: Response): Optional<AuthenticationResult> {
        val tokenAttributes = config.deviceSecretIssuer().introspect(model.deviceSecret)
            .orElseThrow { exceptionFactory.forbiddenException(ErrorCode.INVALID_INPUT, "Invalid Nonce") }
        val subjectMap = (tokenAttributes.getMandatoryAttribute("subject")
            ?.attributeValue as MapAttributeValue).value
            ?: throw exceptionFactory.badRequestException(ErrorCode.INVALID_INPUT, "Missing subject in given data")

        return Optional.of(
            AuthenticationResult(
                AuthenticationAttributes.of(
                    SubjectAttributes.of(subjectMap),
                    ContextAttributes.empty()
                )
            )
        )
    }
}

data class DeviceSecretModel(private val request: Request) {
    val deviceSecret = if (request.isPostRequest) request.getFormParameterValueOrError(DEVICE_SECRET_FIELD_NAME) else null
}

class GetRepresentationFunction : RepresentationFunction {
    companion object {
        val title: Message = Message.ofKey("view.title")
        val viewSubmit: Message = Message.ofKey("view.submit")
        val fieldSecretLabel: Message = Message.ofKey("view.device-secret-label")
        const val TEMPLATE_PATH = "authentication/get"
        const val DEVICE_SECRET_FIELD_NAME = "device_secret"
    }

    override fun apply(model: RepresentationModel, factory: RepresentationFactory): Representation =
        factory.newAuthenticationStep { step ->
            val authUrl = URI.create(model.getString("_authUrl"))
            step.addFormAction(
                HaapiContract.Actions.Kinds.LOGIN, authUrl, HttpMethod.POST,
                MediaType.X_WWW_FORM_URLENCODED, title, viewSubmit
            ) { fields ->
                fields.addTextField(DEVICE_SECRET_FIELD_NAME, fieldSecretLabel)
            }
        }
}