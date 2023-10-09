package com.fooddiary.api.dto.response;

import com.fooddiary.api.entity.image.DayImage;
import com.fooddiary.api.entity.image.Time;
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

    private TimeDetailDTO timeDetail;
    private List<ImageResponseDTO> images;


    @Builder
    @AllArgsConstructor
    @Getter
    @NoArgsConstructor
    public static class ImageResponseDTO {
        private int imageId;
        private byte[] bytes;

        public static ImageResponseDTO createImageResponseDTO(final int imageId, final byte[] bytes) {
            return ImageResponseDTO.builder()
                    .imageId(imageId)
                    .bytes(bytes)
                    .build();
        }

    }

    public static TimeLineResponseDTO TimeLineResponse(final DayImage dayImage, final List<ImageResponseDTO> imageResponseDTOs,
                                                       final int dayOfWeek) {

        final TimeDetailDTO timeDetailDTO = TimeDetailDTO.builder()
                .month(dayImage.getTime().getMonth())
                .day(dayImage.getTime().getDay())
                .dayOfWeek(Time.getDayOfWeek(dayImage.getTime()))
                .build();

        return TimeLineResponseDTO.builder()
                .timeDetail(timeDetailDTO)
                .images(imageResponseDTOs)
                .build();
    }




}
