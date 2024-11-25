package com.ds.deploysurfingbackend.global.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;

@RequiredArgsConstructor
@Component
public class FileReader {

    private final ResourceLoader resourceLoader;

    public byte[] readFileAsBytes(String fileName) {
        try {
            Resource resource = resourceLoader.getResource("classpath:static/" + fileName);
            return FileCopyUtils.copyToByteArray(resource.getInputStream());
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read file: " + fileName, e);
        }
    }

    public String readFileAsString(String fileName) {
        try {
            Resource resource = resourceLoader.getResource("classpath:static/" + fileName);
            try (Reader reader = new InputStreamReader(resource.getInputStream())) {
                return FileCopyUtils.copyToString(reader);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read file: " + fileName, e);
        }
    }

}
