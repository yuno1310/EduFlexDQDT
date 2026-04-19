package com.eduflex.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import java.util.UUID;

@Service
public class SupabaseStorageService {

  @Value("${supabase.url}")
  private String supabaseUrl;

  @Value("${supabase.key}")
  private String supabaseKey;

  public String uploadFile(MultipartFile file, String bucketName) throws Exception {
    String fileExt = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
    String fileName = UUID.randomUUID().toString() + fileExt;

    String endpoint = supabaseUrl + "/storage/v1/object/" + bucketName + "/" + fileName;
    RestTemplate restTemplate = new RestTemplate();
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + supabaseKey);
    headers.set("apikey", supabaseKey);
    headers.setContentType(MediaType.valueOf(file.getContentType()));

    HttpEntity<byte[]> requestEntity = new HttpEntity<>(file.getBytes(), headers);
    ResponseEntity<String> response = restTemplate.exchange(endpoint, HttpMethod.POST, requestEntity, String.class);

    if (response.getStatusCode().is2xxSuccessful()) {
      return supabaseUrl + "/storage/v1/object/public/" + bucketName + "/" + fileName;
    } else {
      throw new Exception("Failed to upload file");
    }
  }
}
