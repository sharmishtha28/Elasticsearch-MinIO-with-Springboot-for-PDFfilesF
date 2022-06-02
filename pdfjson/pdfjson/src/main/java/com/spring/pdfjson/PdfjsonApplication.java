package com.spring.pdfjson;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.pdf.PDFParser;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import com.spring.pdfjson.model.Content;
import com.spring.pdfjson.repository.ContentRepository;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
import io.minio.UploadObjectArgs;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidBucketNameException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.MinioException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;

@SpringBootApplication
@RestController
public class PdfjsonApplication {
	
	@Autowired 
	private ContentRepository contentRepository;
	final  static String endPoint = "http://127.0.0.1:9000";
    final  static String accessKey = "minioadmin";
    final  static String secretKey = "minioadmin";
   // final  static String bucketName = "bucket8828";
    final  static String localFileFolder = "D:\\files\\";
    MinioClient minioClient = MinioClient.builder().endpoint(endPoint)
            .credentials(accessKey, secretKey).build();
    Map<String, String> userMetadata = new HashMap<>();
	
    public String saveContent(String filename,String id,String userId) throws Exception {  
		String errorMsg = null;
    	BodyContentHandler contenthandler = new BodyContentHandler();
        String fileToUpload = localFileFolder + filename;
		File f = new File(fileToUpload);		  
	    FileInputStream fstream = new FileInputStream(f);
	    Metadata data1 = new Metadata();
		ParseContext context = new ParseContext();
		PDFParser pdfparser = new PDFParser();
		pdfparser.parse(fstream, contenthandler, data1,context);
		String str = contenthandler.toString();
		String text = str.replaceAll("\\r|\\n", " ");
		Content c = new Content();
		c.id=id;
		c.userId=userId;
		c.filename=filename;
		c.data=text;
		
		try {
		contentRepository.save(c);
		}catch(Exception e) {
			  errorMsg=" ";
		}
		return errorMsg;
	}
    

	@PostMapping("/upload/{fileName}/{userId}")
    public String WriteToMinIO(@PathVariable String fileName,@PathVariable String userId)
            throws Exception {
		String bucketName=userId;
		String uniqueID = null;
        try {
            boolean bucketExists = minioClient.bucketExists(BucketExistsArgs.builder()
            		.bucket(bucketName).build());
            if (!bucketExists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }

            String fileToUpload = localFileFolder + fileName;
            uniqueID = UUID.randomUUID().toString();
            userMetadata.put("id", uniqueID);
            UploadObjectArgs args = UploadObjectArgs.builder().bucket(bucketName).object(fileName)
            		.userMetadata(userMetadata).filename(fileToUpload).build();
            minioClient.uploadObject(args);
            String a = saveContent(fileName,uniqueID,userId);
            
            System.out.println(fileToUpload + " successfully uploaded to:");
            System.out.println("   container: " + bucketName);
            System.out.println("   filename: " + fileName);
            System.out.println("   id:" + uniqueID);
            System.out.println();
        } catch (MinioException e) {
            System.out.println("Error occurred: " + e);
            uniqueID="File upload unsuccessfull";
        }
        return uniqueID;
    }

	@GetMapping("/findAll") 
	public Iterable<Content> findAllContent() { 
		return contentRepository.findAll(); 
		}

	@GetMapping("/searchByKeyword/{keyword}/{userId}") 
	public List<Content> searchByKeyword(@PathVariable String keyword, @PathVariable String userId){ 
		  return contentRepository.searchInBody(keyword,userId);
	}
	
	@GetMapping("/searchByUserId/{userId}") 
	public List<Content> searchByUserId(@PathVariable String userId){ 
		  return contentRepository.findByUserId(userId);
	}
	
	@DeleteMapping("/deleteById/{id}/{userId}")
	public String deleteById(@PathVariable String id,@PathVariable String userId) throws InvalidKeyException, ErrorResponseException,
		IllegalArgumentException, InsufficientDataException, InternalException, InvalidBucketNameException,
		InvalidResponseException, NoSuchAlgorithmException, ServerException, XmlParserException, IOException {
		
		String errorMsg = null;
		String filename=null;
		String bucketName=userId;

		List<Content> list=new ArrayList<>();
		list.addAll(contentRepository.findAll(PageRequest.of(0, 10)).toList());
		for (Content itr : list) {
	        if (itr.getId().equals(id)) {
	            filename=itr.getFilename();
	        }
	    }
		
        boolean bucketExists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        if (bucketExists) {
        	minioClient.removeObject(
    			    RemoveObjectArgs.builder().bucket(bucketName).object(filename).build());
        }

        try {
        	contentRepository.deleteById(id);
        	}catch(Exception e) {
    			  errorMsg=" ";
        }
    	return errorMsg;
	}
	

	public static void main(String[] args) {
		SpringApplication.run(PdfjsonApplication.class, args);
	}

}
