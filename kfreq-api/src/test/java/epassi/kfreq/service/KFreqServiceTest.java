package epassi.kfreq.service;

import epassi.kfreq.model.FrequencyRecord;
import org.junit.jupiter.api.*;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;

@RunWith(SpringRunner.class)
@DisplayName("Service test cases")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class KFreqServiceTest {

  private Object wordsDictionary;
  private Method addWordMethod;
  private Method resultMethod;
  private Method clearMethod;

  @BeforeAll
  public void beforeAll() throws Exception {
    Class<?> wordsDictionaryClass = KFreqService.class.getDeclaredClasses()[0];
    Constructor<?> constructor = wordsDictionaryClass.getDeclaredConstructors()[0];
    constructor.setAccessible(true);
    // fill stop words set with two elements
    wordsDictionary = constructor.newInstance(5, 2, 15,
        new HashSet<String>(Arrays.asList("the", "from")));

    addWordMethod = wordsDictionaryClass.getDeclaredMethod("addWord", String.class);
    resultMethod = wordsDictionaryClass.getDeclaredMethod("result");
    clearMethod = wordsDictionaryClass.getDeclaredMethod("clear");
  }

  @BeforeEach
  public void beforeEach() throws Exception {
    clearMethod.invoke(wordsDictionary);
  }

  @Test
  @DisplayName("An addition of word from stop list should be ignored")
  void additionWordFromStopListShouldBeIgnored() throws Exception {
    clearMethod.invoke(wordsDictionary);

    addWordMethod.invoke(wordsDictionary, "good");
    addWordMethod.invoke(wordsDictionary, "word");
    assertThat((List<FrequencyRecord>) resultMethod.invoke(wordsDictionary), hasSize(2));

    addWordMethod.invoke(wordsDictionary, "the");
    assertThat((List<FrequencyRecord>) resultMethod.invoke(wordsDictionary), hasSize(2));
  }

  @Test
  @DisplayName("Addition of duplicate should keep list size and increase the word count")
  void additionDuplicateShouldKeepListSize() throws Exception {
    clearMethod.invoke(wordsDictionary);

    addWordMethod.invoke(wordsDictionary, "good");
    addWordMethod.invoke(wordsDictionary, "word");
    assertThat((List<FrequencyRecord>) resultMethod.invoke(wordsDictionary), hasSize(2));

    addWordMethod.invoke(wordsDictionary, "good");
    List<FrequencyRecord> result = (List<FrequencyRecord>) resultMethod.invoke(wordsDictionary);
    assertThat(result, hasSize(2));
    assertThat(result, hasItem(new FrequencyRecord("good", 2)));
  }

  @Test
  @DisplayName("Words with hyphen in the beginning or in the end of the word should not pass filter")
  void additionDuplicateShouldKeepListSiffze() throws Exception {
    clearMethod.invoke(wordsDictionary);

    addWordMethod.invoke(wordsDictionary, "good");
    addWordMethod.invoke(wordsDictionary, "word");
    assertThat((List<FrequencyRecord>) resultMethod.invoke(wordsDictionary), hasSize(2));

    addWordMethod.invoke(wordsDictionary, "-abc");
    assertThat((List<FrequencyRecord>) resultMethod.invoke(wordsDictionary), hasSize(2));

    addWordMethod.invoke(wordsDictionary, "abc-");
    assertThat((List<FrequencyRecord>) resultMethod.invoke(wordsDictionary), hasSize(2));

    addWordMethod.invoke(wordsDictionary, "look-up");
    assertThat((List<FrequencyRecord>) resultMethod.invoke(wordsDictionary),
        hasItem(new FrequencyRecord("look-up", 1)));
  }
}