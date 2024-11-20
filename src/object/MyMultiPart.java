package object;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import jakarta.servlet.http.Part;

public class MyMultiPart {
    private byte[] bytes;
    private String filename;

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public MyMultiPart(byte[] bytes) {
        this.bytes = bytes;
    }

    public MyMultiPart(Part part) throws IOException{
        this.bytes=part.getInputStream().readAllBytes();
        this.filename=getOriginalFilename(part);
    }

    public MyMultiPart() {
    }

    // Écrit le fichier sur le disque à l'emplacement spécifié
    public void transferTo(File dest) throws IOException {
        try (FileOutputStream outputStream = new FileOutputStream(dest)) {
            outputStream.write(bytes);
        }
    }

    // Écrit le fichier dans un dossier en utilisant le nom de fichier spécifié
    public void transferTo(String destDirectory) throws IOException {
        if (filename != null && !filename.isEmpty()) {
            File file = new File(destDirectory, this.getFilename());
            transferTo(file);
        } else {
            throw new IOException("Le nom du fichier est introuvable.");
        }
    }

    public String getOriginalFilename(Part p) {
        String contentDisposition = p.getHeader("content-disposition");
        if (contentDisposition != null) {
            for (String cd : contentDisposition.split(";")) {
                if (cd.trim().startsWith("filename")) {
                    return cd.substring(cd.indexOf('=') + 1).trim().replace("\"", "");
                }
            }
        }
        return null;
    }
}