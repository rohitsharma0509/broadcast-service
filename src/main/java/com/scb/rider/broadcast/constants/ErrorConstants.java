package com.scb.rider.broadcast.constants;
import java.net.URI;

public final class ErrorConstants {

    public static final String ERR_CONCURRENCY_FAILURE = "error.concurrencyFailure";
    public static final String ERR_VALIDATION = "error.validation";
    public static final String PROBLEM_BASE_URL = "https://www.jhipster.tech/problem";
    public static final URI DEFAULT_TYPE = URI.create(PROBLEM_BASE_URL + "/problem-with-message");
    public static final URI CONSTRAINT_VIOLATION_TYPE = URI.create(PROBLEM_BASE_URL + "/constraint-violation");
    public static final URI INVALID_PASSWORD_TYPE = URI.create(PROBLEM_BASE_URL + "/invalid-password");
    public static final URI EMAIL_ALREADY_USED_TYPE = URI.create(PROBLEM_BASE_URL + "/email-already-used");
    public static final URI LOGIN_ALREADY_USED_TYPE = URI.create(PROBLEM_BASE_URL + "/login-already-used");

    private ErrorConstants() {
    }

    public static final String TYPE_MISMATCH_EX_MSG = "api.rider.profile.typeMisMatch.msg";
    public static final String MISSING_PART_EX_MSG = "api.rider.profile.missingPart.msg";
    public static final String MISSING_PARAM_EX_MSG = "api.rider.profile.missingRequestParameter.msg";
    public static final String ARGUMENT_MISMATCH_EX_MSG = "api.rider.profile.argumentMismatch.msg";
    public static final String NO_HANDLER_EX_MSG = "api.rider.profile.noHandler.msg";
    public static final String NO_HTTP_METHOD_EX_MSG = "api.rider.profile.noHttpMethod.msg";
    public static final String MEDIA_NOT_SUPPORT_EX_MSG = "api.rider.profile.mediaNotSupport.msg";
    public static final String SERVER_ERROR_EX_MSG = "api.rider.profile.serverError.msg";
    public static final String RESOURCE_ACCESS_MSG = "api.rider.broadcast.resource.msg";



}
