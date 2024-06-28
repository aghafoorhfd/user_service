package com.pmo.userservice.application.integration.config.feign.decoder;

import static com.pmo.userservice.application.integration.utils.Constants.JSON_PARSING_EXCEPTION_ERROR_MESSAGE;
import static java.util.Optional.ofNullable;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pmo.common.dto.ApiResponseDTO;
import com.pmo.common.dto.ErrorInfoDTO;
import com.pmo.common.enums.PmoErrors;
import com.pmo.common.exception.ApplicationException;
import com.pmo.common.exception.ConflictException;
import com.pmo.common.exception.FiegnClientException;
import feign.Response;
import feign.Util;
import feign.codec.ErrorDecoder;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomErrorDecoder implements ErrorDecoder {

  private final ObjectMapper objectMapper;

  /**
   * {@inheritDoc}
   */
  @Override
  public Exception decode(String methodKey, Response response) {
    try {
      log.info("Decoding error of feign client.");
      String details = ofNullable(response.body()).map(this::bodyToErrorModelResponse)
          .orElse(response.reason());

      ApiResponseDTO exceptionModel = objectMapper.readValue(details, ApiResponseDTO.class);
      List<ErrorInfoDTO> errors = exceptionModel.getErrors();
      Throwable cause = new Default().decode(methodKey, response);
      String errorCause = String.format("An error occurred while calling %s: %s", methodKey,
          cause.getMessage());
      log.error(errorCause);

      if (Boolean.FALSE.equals(errors.isEmpty())) {
        ErrorInfoDTO errorInfoDTO = errors.get(0);

        if (errorInfoDTO.getCode().equals(PmoErrors.UNAUTHORIZED.getCode())) {
          FiegnClientException fiegnClientException = new FiegnClientException(
              errorInfoDTO.getMessage());
          log.error("Unauthorized access while decoding exception: {}", errorInfoDTO.getMessage());
          fiegnClientException.setCode(PmoErrors.UNAUTHORIZED.getCode());
          return fiegnClientException;

        } else if (errorInfoDTO.getCode().equals(PmoErrors.ALREADY_EXISTS.getCode())) {
          ConflictException conflictException = new ConflictException(errorInfoDTO.getMessage());
          conflictException.setCode(PmoErrors.ALREADY_EXISTS.getCode());
          log.error("User already exist while decoding exception: {}", errorInfoDTO.getMessage());
          return conflictException;

        } else {
          ApplicationException applicationException = new ApplicationException(
              errorInfoDTO.getMessage());
          applicationException.setCode(PmoErrors.INTERNAL_SERVER_ERROR.getCode());
          log.error("Internal server error occurred while decoding exception: {}",
              errorInfoDTO.getMessage());
          return applicationException;
        }
      }
      return new ApplicationException(errorCause);
    } catch (JsonProcessingException e) {
      log.error("Exception occurred while Json Processing: ", e);
      throw new ApplicationException(
          PmoErrors.INTERNAL_SERVER_ERROR, String.format(JSON_PARSING_EXCEPTION_ERROR_MESSAGE));
    } catch (Exception e) {
      log.error("Exception occurred while handling Error Response: ", e);
      return new FiegnClientException("Exception occurred while handling Error Response.");
    }
  }

  /**
   * It converts a response body into a string
   *
   * @param body the response body
   * @return the response is converted into a string
   */
  private String bodyToErrorModelResponse(Response.Body body) {
    try {
      return Util.toString(body.asReader(Charset.defaultCharset()));
    } catch (IOException ex) {
      log.error("IO exception {} ", "context", ex);
      return ex.getMessage();
    }
  }
}
