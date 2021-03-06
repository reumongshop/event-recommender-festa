package com.festa.service;

import static com.festa.common.RedisCacheKey.CATEGORY_LIST;
import com.festa.dao.EventDAO;
import com.festa.dto.EventDTO;
import com.festa.model.PageInfo;
import com.festa.model.Participants;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Log4j2
@RequiredArgsConstructor
public class EventService {

    private final EventDAO eventDAO;

    @Cacheable(key = "#categoryCode", value = CATEGORY_LIST, cacheManager = "redisCacheManager")
    public List<EventDTO> getListOfEvents(PageInfo pageInfo, int categoryCode) {
        return eventDAO.getListOfEvents(pageInfo, categoryCode);
    }

    public EventDTO getInfoOfEvent(long eventNo) {
        return eventDAO.getInfoOfEvent(eventNo);
    }

    @Transactional
    @CacheEvict(key = "#categoryCode", value = CATEGORY_LIST, cacheManager = "redisCacheManager")
    public void registerEvents(EventDTO eventDTO, int categoryCode) {
        EventDTO eventInfo = eventDTO.toEntityForInfo();
        eventDAO.registerEvents(eventInfo);

        EventDTO eventAddress = EventDTO.builder()
                .eventNo(eventInfo.getEventNo())
                .cityName(eventDTO.getCityName())
                .districtName(eventDTO.getDistrictName())
                .streetCode(eventDTO.getStreetCode())
                .streetName(eventDTO.getStreetName())
                .detail(eventDTO.getDetail())
                .build();

        eventDAO.registerEventsAddress(eventAddress);
    }

    @Transactional
    public void modifyEventsInfo(EventDTO eventDTO) {
        EventDTO eventInfo = eventDTO.toEntityForInfo();
        eventDAO.modifyEventsInfo(eventInfo);

        EventDTO eventAddress = eventDTO.toEntityForEventAddress();
        eventDAO.modifyEventsAddress(eventAddress);
    }

    public boolean isEventExists(String eventTitle, String startDate) {
        return eventDAO.isEventExists(eventTitle, startDate);
    }

    @Transactional
    public void applyForEvents(Participants participants) throws IllegalStateException {
        EventDTO participantInfo = eventDAO.checkNoOfParticipants(participants.getEventNo());

        if(participantInfo.getParticipantLimit() == participantInfo.getNoOfParticipants()) {
            throw new IllegalStateException("이미 선착순 마감된 이벤트 입니다.");
        }

        eventDAO.applyForEvents(participants);

        Participants participantAddress = participants.toEntityForAddress();
        eventDAO.insertParticipantAddress(participantAddress);

        eventDAO.increaseParticipants(participants.getEventNo());
    }

    @Transactional
    public void cancelEvent(Participants participants) throws IllegalStateException {
        eventDAO.cancelEvent(participants.getUserNo());

        boolean isParticipated = eventDAO.isParticipated(participants.getUserNo());

        if(!isParticipated) {
            throw new IllegalStateException("접수한 이벤트가 아닙니다");
        }

        eventDAO.reduceParticipants(participants.getEventNo());
    }

    public Participants getParticipantList(long userNo, Participants participants) {

        if(userNo != participants.getUserNo()) {
            throw new IllegalStateException("이벤트를 등록한 주최자만 조회가 가능합니다.");
        }

        return eventDAO.getParticipantList(participants);
    }

    @Transactional
    public void deleteEventNo(long eventNo, long userNo) {
        EventDTO eventInfo = eventDAO.getInfoOfEvent(eventNo);

        if(eventInfo.getUserNo() != userNo) {
            throw new IllegalStateException("해당 이벤트를 등록한 사용자가 아닙니다");
        }

        eventDAO.deleteEvent(eventNo);
        eventDAO.deleteEventAddress(eventNo);
    }
}
