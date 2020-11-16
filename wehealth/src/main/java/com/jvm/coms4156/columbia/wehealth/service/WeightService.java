package com.jvm.coms4156.columbia.wehealth.service;

import com.jvm.coms4156.columbia.wehealth.dto.WeightHistoryDetailsDto;
import com.jvm.coms4156.columbia.wehealth.dto.WeightHistoryResponseDto;
import com.jvm.coms4156.columbia.wehealth.dto.WeightRecordDto;
import com.jvm.coms4156.columbia.wehealth.dto.UserIdDto;
import com.jvm.coms4156.columbia.wehealth.entity.DBUser;
import com.jvm.coms4156.columbia.wehealth.entity.WeightHistory;
import com.jvm.coms4156.columbia.wehealth.exception.BadRequestException;
import com.jvm.coms4156.columbia.wehealth.exception.NotFoundException;
import com.jvm.coms4156.columbia.wehealth.repository.*;
import com.jvm.coms4156.columbia.wehealth.utility.Utility;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static com.jvm.coms4156.columbia.wehealth.common.Constants.*;

@Service
@Log4j2
public class WeightService {

    @Autowired
    private DBUserRepository dbUserRepo;

    @Autowired
    private WeightHistoryRepository weightHistoryRepo;

    @Autowired
    private NutrientTypeRepository nutrientTypeRepo;

    @Transactional
    public void addWeightRecordToDB(WeightRecordDto weightRecordDto) {
        // add weight record to weight_history table
        WeightHistory weightHistory = new WeightHistory();
        Optional<DBUser> user = dbUserRepo.findByUserId(weightRecordDto.getUserId());
        if (user.isEmpty()) {
            throw new NotFoundException("User not found with provided user id.");
        }
        weightHistory.setUser(user.get());

        // In weight_history table, the default unit of weight is gram
        weightHistory.setUnit(GRAM);

        if (weightRecordDto.getUnit().toLowerCase().equals(POUND)) {
            weightHistory.setWeight(weightRecordDto.getWeight() * POUND_TO_GRAM);
        }
        else if (weightRecordDto.getUnit().toLowerCase().equals(GRAM)) {
            weightHistory.setWeight(weightRecordDto.getWeight());
        }
        else {
            throw new BadRequestException("Illegal weight unit, please use gram or pound.");
        }

        String currentDateTime = Utility.getStringOfCurrentDateTime();
        weightHistory.setCreatedBy(user.get().getUsername());
        weightHistory.setCreatedTime(currentDateTime);
        weightHistory.setUpdatedBy(user.get().getUsername());
        weightHistory.setUpdatedTime(currentDateTime);

        weightHistoryRepo.save(weightHistory);

    }

    public WeightHistoryResponseDto getWeightHistory(UserIdDto userIdDto,
                                                 Optional<String> unit, Optional<Integer> length) {
        Optional<DBUser> user = dbUserRepo.findByUserId(userIdDto.getUserId());
        if (user.isEmpty()) {
            throw new NotFoundException("User not found with provided user id.");
        }

        String timeUnit = unit.orElse(ALL); // Default: find all weight history
        int timeLength = length.orElse(ONE); // Default: 1 time unit e.g. 1 week, 1 month...
        if (timeLength < 0) {
            throw new BadRequestException("Invalid time length: Duration must be positive.");
        }

        List<WeightHistory> weightHistoryList;
        log.info("**********Get weight history by selected duration**********");
        if (timeUnit.equals(ALL)) {
            weightHistoryList = weightHistoryRepo.findAllByUser(user.get());
        }
        else {
            // Calculate starting datetime for weight history by selected duration
            String startDateTime = Utility.getStringOfStartDateTime(timeUnit, timeLength);
            weightHistoryList = weightHistoryRepo.findAllByUserAndCreatedTimeAfter(user.get(), startDateTime);
        }
        WeightHistoryResponseDto weightHistoryResponseDto = new WeightHistoryResponseDto();
        log.info("**********Add every weight history to responseDto**********");
        for (WeightHistory weightHistory: weightHistoryList) {
            WeightHistoryDetailsDto weightHistoryDetailsDto = getWeightHistoryDetails(weightHistory);
            weightHistoryResponseDto.getWeightHistoryList().add(weightHistoryDetailsDto);
        }
        return weightHistoryResponseDto;
    }

    private WeightHistoryDetailsDto getWeightHistoryDetails(WeightHistory weightHistory) {
        WeightHistoryDetailsDto weightHistoryDetailsDto = new WeightHistoryDetailsDto();
        weightHistoryDetailsDto.setWeightHistoryId(weightHistory.getWeightHistoryId());
        weightHistoryDetailsDto.setWeight(weightHistory.getWeight());
        weightHistoryDetailsDto.setUnit(weightHistory.getUnit()); // default: gram
        weightHistoryDetailsDto.setTime(weightHistory.getCreatedTime());
        return weightHistoryDetailsDto;
    }

    @Transactional
    public void editWeightRecord(String weightId, WeightRecordDto weightRecordDto) {
        Optional<WeightHistory> weightHistory = weightHistoryRepo.findById(weightId);
        if(weightHistory.isEmpty()) {
            throw new NotFoundException("Weight record not found with provided weight record id.");
        }

        WeightHistory weightHistoryRecord = weightHistory.get();
        if(! weightHistoryRecord.getUser().getUser_id().equals(weightRecordDto.getUserId())) {
            throw new BadRequestException("Illegal edit attempt: Record not belong to this user.");
        }

        String currentDateTime = Utility.getStringOfCurrentDateTime();
        weightHistoryRecord.setWeight(weightRecordDto.getWeight());
        weightHistoryRecord.setUnit(weightRecordDto.getUnit());
        weightHistoryRecord.setUpdatedTime(currentDateTime);
        weightHistoryRepo.save(weightHistoryRecord);
    }

    @Transactional
    public void deleteWeightRecord(String weightId, UserIdDto userIdDto) {
        Optional<WeightHistory> weightHistory = weightHistoryRepo.findById(weightId);
        if(weightHistory.isEmpty()) {
            throw new NotFoundException("Weight record not found with provided weight record id.");
        }

        WeightHistory weightHistoryRecord = weightHistory.get();
        if(! weightHistoryRecord.getUser().getUser_id().equals(userIdDto.getUserId())) {
            throw new BadRequestException("Illegal delete attempt: Record not belong to this user.");
        }

        weightHistoryRepo.deleteById(weightId);
    }

}
