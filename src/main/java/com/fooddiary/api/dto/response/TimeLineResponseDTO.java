package com.fooddiary.api.dto.response;

import com.fooddiary.api.entity.image.DayImage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class TimeLineResponseDTO {

    private int month;
    private int day;
    private String dayOfWeek;
    private List<TimeLineImageResponseDTO> images;


    @Builder
    @AllArgsConstructor
    @Getter
    @NoArgsConstructor
    public static class TimeLineImageResponseDTO {
        private int id;
        private byte[] bytes;

        public static TimeLineImageResponseDTO TimeLineImageResponse(final int imageId, final byte[] bytes) {
            return TimeLineImageResponseDTO.builder()
                    .id(imageId)
                    .bytes(bytes)
                    .build();
        }

    }

    public static TimeLineResponseDTO TimeLineResponse(final DayImage dayImage, final List<TimeLineImageResponseDTO> timeLineImageResponseDtoS,
                                                       final int dayOfWeek) {

        final String dayOfWeekString = getDayOfWeek(dayOfWeek);

        return TimeLineResponseDTO.builder()
                .month(dayImage.getTime().getMonth())
                .day(dayImage.getTime().getDay())
                .dayOfWeek(dayOfWeekString)
                .images(timeLineImageResponseDtoS)
                .build();
    }

    public static String getDayOfWeek(final int dayOfWeek) {
        switch (dayOfWeek) {
            case 1:
                return "월";
            case 2:
                return "화";
            case 3:
                return "수";
            case 4:
                return "목";
            case 5:
                return "금";
            case 6:
                return "토";
            case 7:
                return "일";
            default:
                throw new RuntimeException("요일을 찾을 수 없습니다.");
        }
    }


}
