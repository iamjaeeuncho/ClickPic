package com.phoenix.clickpic.photo.controller;

import java.io.ByteArrayInputStream;
import java.util.Base64;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.phoenix.clickpic.photo.model.Photo;
import com.phoenix.clickpic.photo.service.PhotoService;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

@Slf4j
@RestController
@RequestMapping("/photo")
public class PhotoController {

    private final S3Client s3Client;
    
    @Autowired
    private PhotoService photoService;
    
    @Value("${aws.s3.bucket.name}")
    private String bucketName;

    @Value("${aws.s3.bucket.url}")
    private String bucketUrl;

    public PhotoController(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    @PostMapping("/save")
    public ResponseEntity<String> savePhoto(@RequestBody Photo photo) {
    	
        String base64Image = photo.getOriginPhoto();
        Long roomId = photo.getRoomId();

        try {
            // 데이터 URL 스킴 부분 제거
            String base64Data = base64Image.replaceFirst("^data:image\\/[^;]+;base64,", "");

            // Base64 문자열을 바이트 배열로 디코딩
            byte[] imageData = Base64.getDecoder().decode(base64Data);

            // S3에 업로드할 파일 이름 생성
            String fileName = "originPhoto_" + roomId + ".png";

            // S3에 업로드
            try (ByteArrayInputStream inputStream = new ByteArrayInputStream(imageData)) {
                PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(fileName)
                        .build();

                // 이미지를 업로드
                PutObjectResponse response = s3Client.putObject(putObjectRequest, software.amazon.awssdk.core.sync.RequestBody.fromInputStream(inputStream, imageData.length));
            }

            // 저장된 이미지의 URL 생성
            String imageUrl = bucketUrl + "/" + fileName;
            
            photo.setRoomId(roomId);
            photo.setOriginPhoto(imageUrl);

            photoService.saveOriginPhoto(photo);
            
            return ResponseEntity.ok(imageUrl);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("이미지 저장에 실패했습니다.");
        }
    }
    
    @PostMapping("/complete")
    public ResponseEntity<String> updatePhoto(@RequestParam("roomSession") String roomSession,
                            @RequestBody Photo photo) {

      String originPhoto = photo.getOriginPhoto();
      Long roomId = photo.getRoomId();
      
      try {
          // 데이터 URL 스킴 부분 제거
          String base64Data = originPhoto.replaceFirst("^data:image\\/[^;]+;base64,", "");

          // Base64 문자열을 바이트 배열로 디코딩
          byte[] imageData = Base64.getDecoder().decode(base64Data);

          // S3에 업로드할 파일 이름 생성
          String fileName = "completePhoto_" + roomId + ".png";

          // S3에 업로드
          try (ByteArrayInputStream inputStream = new ByteArrayInputStream(imageData)) {
              PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                      .bucket(bucketName)
                      .key(fileName)
                      .build();

              // 이미지를 업로드
              PutObjectResponse response = s3Client.putObject(putObjectRequest, software.amazon.awssdk.core.sync.RequestBody.fromInputStream(inputStream, imageData.length));
          }

          // 저장된 이미지의 URL 생성
          String imageUrl = bucketUrl + "/" + fileName;
          
          photoService.updateCompletePhoto(roomId, imageUrl);

          return ResponseEntity.ok(imageUrl);
      } catch (Exception e) {
          e.printStackTrace();
          return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("이미지 저장에 실패했습니다.");
      }
    }
    
    @PostMapping("/final")
    public void completePhoto(@RequestParam("roomSession") String roomSession,
                              @RequestBody Photo photo) {
      Photo completePhoto = photoService.getPhotoBySession(roomSession);
      
      Long roomId = completePhoto.getRoomId();
      String isPublic = photo.getIsPublic();
      String note = photo.getNote();
      
      // 데이터 로그 출력
      System.out.println("roomId: " + roomId);
      System.out.println("isPublic: " + isPublic);
      System.out.println("note: " + note);
      
      photoService.updateFinalPhoto(roomId, isPublic, note);
    }
    
    @GetMapping("/info")
    public Photo getRoomOriginPhoto(@RequestParam("roomSession") String roomSession) {
    	System.out.println("roomSession"+roomSession);
      Photo photo = photoService.getPhotoBySession(roomSession);
      System.out.println("photo info"+photo);
      System.out.println("photo info"+photo.getOriginPhoto());
    
      return photo;
    }
    
    @GetMapping("/hot")
    public List<Photo> hotPhotos() {
      try {
        System.out.println("hothothothot");
        return photoService.selectHotPhotos();
      } catch (Exception e) {
          log.error("Error occurred while fetching hot photos", e);
          throw e;
      }
    }
}
