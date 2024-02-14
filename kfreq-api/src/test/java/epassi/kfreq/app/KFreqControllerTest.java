package epassi.kfreq.app;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import epassi.kfreq.controller.KFreqController;
import epassi.kfreq.dao.S3ObjectsDao;
import epassi.kfreq.model.TopBodyRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.io.ByteArrayInputStream;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@RunWith(SpringRunner.class)
@WebMvcTest(KFreqController.class)
@DisplayName("Controller test cases")
public class KFreqControllerTest {

  @Autowired
  private MockMvc mockMvc;

  ObjectMapper objectMapper = new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL);

  @MockBean
  private S3ObjectsDao s3Dao;

  @Test
  @DisplayName("Top rated word should be first")
  void topCountWithoutStopWordsFilterIsOk() throws Exception {
    Mockito.when(s3Dao.getObjectInputStream("https://foo-url"))
        .thenReturn(new ByteArrayInputStream("This example, and that example and, other example!".getBytes("UTF-8")));

    String json = objectMapper.writeValueAsString(
        new TopBodyRequest(
          "https://foo-url",
          10,
          "UTF-8",
          false));
    int expectedTopCount = 3;

    mockMvc.perform(post("/api/v1/top")
            .contentType("application/json")
            .content(json)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].word", is("example")))
        .andExpect(jsonPath("$[0].count", is(expectedTopCount)));
  }

  @Test
  @DisplayName("Result list size should not exceed 'limit' value")
  void topCountLimitWorksCorrect() throws Exception {
    Mockito.when(s3Dao.getObjectInputStream("https://foo-url"))
        .thenReturn(new ByteArrayInputStream("This example, and that example and, other example!".getBytes("UTF-8")));

    String jsonLimitHigh = objectMapper.writeValueAsString(
        new TopBodyRequest(
            "https://foo-url",
            50,
            "UTF-8",
            false));
    int expectedTopCountHigh = 5;

    String jsonLimitLow = objectMapper.writeValueAsString(
        new TopBodyRequest(
            "https://foo-url",
            3,
            "UTF-8",
            false));
    int expectedTopCountLimited = 3;

    mockMvc.perform(post("/api/v1/top")
            .contentType("application/json")
            .content(jsonLimitHigh)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(expectedTopCountHigh)));

    mockMvc.perform(post("/api/v1/top")
            .contentType("application/json")
            .content(jsonLimitLow)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(expectedTopCountLimited)));
  }
  @Test
  @DisplayName("Stop words list should filter insignificant words")
  void topCountStopWordsFilteringIsOk() throws Exception {
    Mockito.when(s3Dao.getObjectInputStream("https://foo-url"))
        .thenReturn(new ByteArrayInputStream("This example, and that example and, other example!".getBytes("UTF-8")));

    String json = objectMapper.writeValueAsString(
        new TopBodyRequest(
            "https://foo-url",
            10,
            "UTF-8",
            true));
    int expectedTopCountFiltered = 2; // for ["example", "other"] - conjunctions, pronouns and prepositions are excluded

    mockMvc.perform(post("/api/v1/top")
            .contentType("application/json")
            .content(json)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(2)));
  }

  @Test
  @DisplayName("Optional fields absence should not fail request")
  void topCountOptionalFieldsShouldNotFail() throws Exception {
    Mockito.when(s3Dao.getObjectInputStream("https://foo-url"))
        .thenReturn(new ByteArrayInputStream("This example, and that example and, other example!".getBytes("UTF-8")));

    String jsonMandatoryOk = "{\"resourceUrl\":\"https://foo-url\",\"limit\":5}";

    mockMvc
        .perform(post("/api/v1/top")
            .contentType("application/json")
            .content(jsonMandatoryOk))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("Mandatory fields absence should fail request")
  void topCountMandatoryFieldsShouldFail() throws Exception {
    Mockito.when(s3Dao.getObjectInputStream("https://foo-url"))
        .thenReturn(new ByteArrayInputStream("This example, and that example and, other example!".getBytes("UTF-8")));

    String jsonMandatoryLimitFail = "{\"resourceUrl\":\"https://foo-url\"}";
    String jsonMandatoryResourceFail = "{\"limit\":8}";

    mockMvc
        .perform(post("/api/v1/top")
            .contentType("application/json")
            .content(jsonMandatoryLimitFail))
        .andExpect(status().isBadRequest());

    mockMvc
        .perform(post("/api/v1/top")
            .contentType("application/json")
            .content(jsonMandatoryResourceFail))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("Status request should always work well")
  void statusIsOk() throws Exception {
    mockMvc.perform(get("/api/v1/status"))
        .andExpect(status().isOk());
  }
}