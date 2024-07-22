package travility_back.travility.util;

import org.springframework.web.multipart.MultipartFile;
import travility_back.travility.config.UploadInform;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class FileUploadUtil {

    private static String path = UploadInform.uploadPath;

    //이미지 업로드
    public static String uploadImage(MultipartFile img) throws IOException {
        if (img == null || img.isEmpty()){
            throw new IllegalArgumentException("File cannot be null or empty");
        }

        //이미지 로컬 서버에 업로드
        String originalName = img.getOriginalFilename(); //파일 원본 이름
        String extension = originalName.substring(originalName.indexOf(".")); //파일 확장자
        String newImgName = UUID.randomUUID().toString() + extension; //새 이미지 이름
        img.transferTo(new File(path, newImgName)); //지정된 경로를 가진 새 파일 객체 생성하여 업로드

        return newImgName;
    }

    //이미지 삭제
    public static void deleteImage(String imgName){
        File oldImg = new File(path, imgName);
        if (oldImg.exists()) {
            oldImg.delete();
        }
    }
}
