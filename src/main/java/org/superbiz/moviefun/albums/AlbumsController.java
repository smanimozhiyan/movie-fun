package org.superbiz.moviefun.albums;

import org.apache.commons.io.IOUtils;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.superbiz.moviefun.blobstore.Blob;
import org.superbiz.moviefun.blobstore.BlobStore;
import org.superbiz.moviefun.blobstore.FileStore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;

import static java.lang.ClassLoader.getSystemResource;
import static java.lang.String.format;
import static java.nio.file.Files.readAllBytes;

@Controller
@RequestMapping("/albums")
public class AlbumsController {

    private final Logger log = LoggerFactory.getLogger(AlbumsController.class);
    private final AlbumsBean albumsBean;
    private final BlobStore blobStore;

    public AlbumsController(AlbumsBean albumsBean, BlobStore blobStore) {
        this.albumsBean = albumsBean;
        this.blobStore = blobStore;
    }


    @GetMapping
    public String index(Map<String, Object> model) {
        model.put("albums", albumsBean.getAlbums());
        return "albums";
    }

    @GetMapping("/{albumId}")
    public String details(@PathVariable long albumId, Map<String, Object> model) {
        model.put("album", albumsBean.find(albumId));
        return "albumDetails";
    }

    @PostMapping("/{albumId}/cover")
    public String uploadCover(@PathVariable long albumId, @RequestParam("file") MultipartFile uploadedFile) throws IOException {
        saveUploadToFile(uploadedFile, new File(String.valueOf(albumId)));

        return format("redirect:/albums/%d", albumId);
    }

    @GetMapping("/{albumId}/cover")
    public HttpEntity<byte[]> getCover(@PathVariable long albumId) throws IOException, URISyntaxException {
        log.info("Enters getcover method with album id : " + albumId);
//        Path coverFilePath = getExistingCoverPath(albumId);
//        byte[] imageBytes = readAllBytes(coverFilePath);
        Optional<Blob> blob = blobStore.get(String.valueOf(albumId));
        byte[] imageBytes = null;
        if(blob.isPresent()) {
            InputStream inputStream = blob.get().inputStream;
            log.info("Input stream length : " + inputStream.available());
            imageBytes = IOUtils.toByteArray(inputStream);
            log.info("Byte array length : " + imageBytes.length);
            HttpHeaders headers = createImageHttpHeaders(new File(blob.get().name).toPath(), imageBytes, blob.get().contentType);
            log.info("Returning from getcover method : " + new File(blob.get().name).toPath());
            return new HttpEntity<>(imageBytes, headers);
        }
        return new HttpEntity<>(imageBytes);
    }


    private void saveUploadToFile(@RequestParam("file") MultipartFile uploadedFile, File targetFile) throws IOException {
//        targetFile.delete();
//        targetFile.getParentFile().mkdirs();
//        targetFile.createNewFile();
//
//        try (FileOutputStream outputStream = new FileOutputStream(targetFile)) {
//            outputStream.write(uploadedFile.getBytes());
//        }
        log.info("Enters saveUploadToFile method with file name : " + targetFile);
        blobStore.put(new Blob(targetFile.getName(), uploadedFile.getInputStream(), uploadedFile.getContentType()));
        log.info("Enters saveUploadToFile persisted succesfully : " + targetFile);
    }

    private HttpHeaders createImageHttpHeaders(Path coverFilePath, byte[] imageBytes, String contentType) throws IOException {
//        String contentType = new Tika().detect(coverFilePath);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(contentType));
        headers.setContentLength(imageBytes.length);
        return headers;
    }

//    private File getCoverFile(@PathVariable long albumId) {
//        String coverFileName = format("%d", albumId);
//        return new File(coverFileName);
//    }
//
//    private Path getExistingCoverPath(@PathVariable long albumId) throws URISyntaxException {
//        File coverFile = getCoverFile(albumId);
//        Path coverFilePath;
//
//        if (coverFile.exists()) {
//            coverFilePath = coverFile.toPath();
//        } else {
//            coverFilePath = Paths.get(getSystemResource("default-cover.jpg").toURI());
//        }
//
//        return coverFilePath;
//    }
}
