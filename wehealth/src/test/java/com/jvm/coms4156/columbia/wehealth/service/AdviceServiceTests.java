package com.jvm.coms4156.columbia.wehealth.service;

import static com.jvm.coms4156.columbia.wehealth.common.Constants.WEEK;
import static org.mockito.Mockito.when;

import com.jvm.coms4156.columbia.wehealth.domain.AuthenticatedUser;
import com.jvm.coms4156.columbia.wehealth.dto.*;
import com.jvm.coms4156.columbia.wehealth.entity.WeightHistory;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AdviceServiceTests {
  @InjectMocks
  private AdviceService adviceService;

  @Mock
  private DietService dietService;

  @Mock
  private ExerciseService exerciseService;

  @Mock
  private WeightService weightService;

  private DietHistoryDetailsDto validDietHistory(Integer dietHistoryId, Integer dietTypeId,
                                                 String time) {
    DietHistoryDetailsDto dietHistory = new DietHistoryDetailsDto();
    dietHistory.setDietHistoryId(dietHistoryId);
    dietHistory.setDietTypeId(dietTypeId);
    dietHistory.setTotalProtein(10.0 * dietHistoryId);
    dietHistory.setTotalFat(7.0 * dietHistoryId);
    dietHistory.setTotalCarbs(30.0 * dietHistoryId);
    dietHistory.setTotalCalories(200.0 * dietHistoryId);
    dietHistory.setTime(time);
    return dietHistory;
  }

  private DietHistoryResponseDto getValidDiestHistory(Integer length) {
    List<DietHistoryDetailsDto> dietHistoryList = new ArrayList<>();
    DietHistoryResponseDto dietHistoryResponseDto = new DietHistoryResponseDto();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    // DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
    for (int i = 0; i < length; i++) {
      String day = LocalDateTime.now(ZoneId.systemDefault()).minusDays(i).format(formatter);
      dietHistoryList.add(validDietHistory(i, i, day));
      dietHistoryList.add(validDietHistory(i, i, day));
    }
    dietHistoryResponseDto.setDietHistoryList(dietHistoryList);
    return dietHistoryResponseDto;
  }

  private ExerciseHistoryDetailsDto validExercise(Integer exerciseHistoryId,
                                                  Double duration, String time) {
    ExerciseHistoryDetailsDto exercise = new ExerciseHistoryDetailsDto();
    exercise.setExerciseHistoryId(exerciseHistoryId);
    exercise.setExerciseTypeName("test");
    exercise.setDuration(duration * exerciseHistoryId);
    exercise.setTime(time);
    exercise.setTotalCalories(duration * 5);
    return exercise;
  }

  private ExerciseHistoryResponseDto getValidExerciseHistory(Integer length) {
    List<ExerciseHistoryDetailsDto> exerciseHistoryList = new ArrayList<>();
    ExerciseHistoryResponseDto dto = new ExerciseHistoryResponseDto();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    for (int i = 0; i < length; i++) {
      String day = LocalDateTime.now(ZoneId.systemDefault()).minusDays(i).format(formatter);
      exerciseHistoryList.add(validExercise(i, 10.0, day));
      exerciseHistoryList.add(validExercise(i, 10.0, day));
    }
    dto.setExerciseHistoryList(exerciseHistoryList);
    return dto;
  }

  private WeightHistoryDetailsDto validWeight(Double weight, String time) {
    WeightHistoryDetailsDto weightDto = new WeightHistoryDetailsDto();
    weightDto.setWeightHistoryId(1);
    weightDto.setWeight(weight);
    weightDto.setTime(time);
    weightDto.setUnit("gram");
    return weightDto;
  }

  private WeightHistoryResponseDto getValidWeightHistory(Integer length) {
    List<WeightHistoryDetailsDto> weightHistory = new ArrayList<>();
    WeightHistoryResponseDto dto = new WeightHistoryResponseDto();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    for (int i = 0; i < length; i++) {
      String day = LocalDateTime.now(ZoneId.systemDefault()).minusDays(i).format(formatter);
      weightHistory.add(validWeight(110000.0 + 10 * i, day));
      weightHistory.add(validWeight(115000.0 + 10 * i, day));
    }
    dto.setWeightHistoryList(weightHistory);
    return dto;
  }


  @Test
  public void getAdviceValidLongRecordTest() {
    when(dietService.getDietHistory(Mockito.any(AuthenticatedUser.class),
            Mockito.any(Optional.class), Mockito.any(Optional.class)))
            .thenReturn(getValidDiestHistory(100));

    when(exerciseService.getExerciseHistory(Mockito.any(Optional.class),
            Mockito.any(Optional.class), Mockito.any(AuthenticatedUser.class)))
            .thenReturn(getValidExerciseHistory(100));

    when(weightService.getWeightHistory(Mockito.any(AuthenticatedUser.class),
            Mockito.any(Optional.class), Mockito.any(Optional.class)))
            .thenReturn(getValidWeightHistory(100));

    AuthenticatedUser au = new AuthenticatedUser(1L);
    Optional<String> unit = Optional.of(WEEK);
    Optional<Integer> length = Optional.of(1);
    AdviceDto adviceDto = adviceService.getAdvice(au, length, unit);
    Assertions.assertEquals(false, adviceDto.getIsEmpty());
    Assertions.assertEquals(100, adviceDto.getDietByDate().size());
    Assertions.assertEquals(100, adviceDto.getExerciseByDate().size());
    Assertions.assertEquals(100, adviceDto.getWeightByDate().size());
  }

  @Test
  public void getAdviceValidMidRecordTest() {
    when(dietService.getDietHistory(Mockito.any(AuthenticatedUser.class),
            Mockito.any(Optional.class), Mockito.any(Optional.class)))
            .thenReturn(getValidDiestHistory(10));

    when(exerciseService.getExerciseHistory(Mockito.any(Optional.class),
            Mockito.any(Optional.class), Mockito.any(AuthenticatedUser.class)))
            .thenReturn(getValidExerciseHistory(10));

    when(weightService.getWeightHistory(Mockito.any(AuthenticatedUser.class),
            Mockito.any(Optional.class), Mockito.any(Optional.class)))
            .thenReturn(getValidWeightHistory(10));

    AuthenticatedUser au = new AuthenticatedUser(1L);
    Optional<String> unit = Optional.of(WEEK);
    Optional<Integer> length = Optional.of(1);
    AdviceDto adviceDto = adviceService.getAdvice(au, length, unit);
    Assertions.assertEquals(false, adviceDto.getIsEmpty());
    Assertions.assertEquals(10, adviceDto.getDietByDate().size());
    Assertions.assertEquals(10, adviceDto.getExerciseByDate().size());
    Assertions.assertEquals(10, adviceDto.getWeightByDate().size());
  }

  @Test
  public void getAdviceValidShortRecordTest() {
    when(dietService.getDietHistory(Mockito.any(AuthenticatedUser.class),
            Mockito.any(Optional.class), Mockito.any(Optional.class)))
            .thenReturn(getValidDiestHistory(1));

    when(exerciseService.getExerciseHistory(Mockito.any(Optional.class),
            Mockito.any(Optional.class), Mockito.any(AuthenticatedUser.class)))
            .thenReturn(getValidExerciseHistory(1));

    when(weightService.getWeightHistory(Mockito.any(AuthenticatedUser.class),
            Mockito.any(Optional.class), Mockito.any(Optional.class)))
            .thenReturn(getValidWeightHistory(1));

    AuthenticatedUser au = new AuthenticatedUser(1L);
    Optional<String> unit = Optional.of(WEEK);
    Optional<Integer> length = Optional.of(1);
    AdviceDto adviceDto = adviceService.getAdvice(au, length, unit);
    Assertions.assertEquals(false, adviceDto.getIsEmpty());
    Assertions.assertEquals(1, adviceDto.getDietByDate().size());
    Assertions.assertEquals(1, adviceDto.getExerciseByDate().size());
    Assertions.assertEquals(1, adviceDto.getWeightByDate().size());
  }

  @Test
  public void getAdviceValidCalculationTest() {
    when(dietService.getDietHistory(Mockito.any(AuthenticatedUser.class),
            Mockito.any(Optional.class), Mockito.any(Optional.class)))
            .thenReturn(getValidDiestHistory(1));
    when(exerciseService.getExerciseHistory(Mockito.any(Optional.class),
            Mockito.any(Optional.class), Mockito.any(AuthenticatedUser.class)))
            .thenReturn(getValidExerciseHistory(1));

    when(weightService.getWeightHistory(Mockito.any(AuthenticatedUser.class),
            Mockito.any(Optional.class), Mockito.any(Optional.class)))
            .thenReturn(getValidWeightHistory(1));

    AuthenticatedUser au = new AuthenticatedUser(1L);
    Optional<String> unit = Optional.of(WEEK);
    Optional<Integer> length = Optional.of(1);
    AdviceDto adviceDto = adviceService.getAdvice(au, length, unit);

    Assertions.assertEquals(false, adviceDto.getIsEmpty());
    Assertions.assertEquals(adviceDto.getDietByDate().get(0).getTotalCalories(),
            adviceDto.getAvgCalories());

    Assertions.assertEquals(adviceDto.getExerciseByDate().get(0).getTotalDuration(),
            adviceDto.getAvgDuration());

    Assertions.assertEquals(adviceDto.getWeightByDate().get(0).getWeight(),
            112500.0);
  }

  @Test
  public void getAdviceInvalidDietTest() {
    when(dietService.getDietHistory(Mockito.any(AuthenticatedUser.class),
            Mockito.any(Optional.class), Mockito.any(Optional.class)))
            .thenReturn(new DietHistoryResponseDto());
    when(exerciseService.getExerciseHistory(Mockito.any(Optional.class),
            Mockito.any(Optional.class), Mockito.any(AuthenticatedUser.class)))
            .thenReturn(getValidExerciseHistory(1));
    when(weightService.getWeightHistory(Mockito.any(AuthenticatedUser.class),
            Mockito.any(Optional.class), Mockito.any(Optional.class)))
            .thenReturn(getValidWeightHistory(1));

    AuthenticatedUser au = new AuthenticatedUser(1L);
    Optional<String> unit = Optional.of(WEEK);
    Optional<Integer> length = Optional.of(1);
    AdviceDto adviceDto = adviceService.getAdvice(au, length, unit);
    Assertions.assertEquals(false, adviceDto.getIsEmpty());
  }

  @Test
  public void getAdviceInvalidExerciseTest() {
    when(dietService.getDietHistory(Mockito.any(AuthenticatedUser.class),
            Mockito.any(Optional.class), Mockito.any(Optional.class)))
            .thenReturn(getValidDiestHistory(1));
    when(exerciseService.getExerciseHistory(Mockito.any(Optional.class),
            Mockito.any(Optional.class), Mockito.any(AuthenticatedUser.class)))
            .thenReturn(new ExerciseHistoryResponseDto());
    when(weightService.getWeightHistory(Mockito.any(AuthenticatedUser.class),
            Mockito.any(Optional.class), Mockito.any(Optional.class)))
            .thenReturn(new WeightHistoryResponseDto());

    AuthenticatedUser au = new AuthenticatedUser(1L);
    Optional<String> unit = Optional.of(WEEK);
    Optional<Integer> length = Optional.of(1);
    AdviceDto adviceDto = adviceService.getAdvice(au, length, unit);
    Assertions.assertEquals(false, adviceDto.getIsEmpty());
  }

  @Test
  public void getAdviceInvalidUserTest() {
    when(dietService.getDietHistory(Mockito.any(AuthenticatedUser.class),
            Mockito.any(Optional.class), Mockito.any(Optional.class)))
            .thenReturn(new DietHistoryResponseDto());

    when(exerciseService.getExerciseHistory(Mockito.any(Optional.class),
            Mockito.any(Optional.class), Mockito.any(AuthenticatedUser.class)))
            .thenReturn(new ExerciseHistoryResponseDto());

    when(weightService.getWeightHistory(Mockito.any(AuthenticatedUser.class),
            Mockito.any(Optional.class), Mockito.any(Optional.class)))
            .thenReturn(new WeightHistoryResponseDto());

    AuthenticatedUser au = new AuthenticatedUser(-1L);
    Optional<String> unit = Optional.of(WEEK);
    Optional<Integer> length = Optional.of(1);
    AdviceDto adviceDto = adviceService.getAdvice(au, length, unit);
    Assertions.assertEquals(true, adviceDto.getIsEmpty());
  }
}
