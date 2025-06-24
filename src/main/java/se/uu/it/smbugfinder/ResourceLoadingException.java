package se.uu.it.smbugfinder;

public class ResourceLoadingException extends RuntimeException {

   private static final long serialVersionUID = 1L;

   public ResourceLoadingException(Exception exception) {
       super(exception);
   }

   public ResourceLoadingException(String message) {
       super(message);
   }

   public ResourceLoadingException(String message, Exception exception) {
       super(message, exception);
   }

   @Override
   public String getMessage() {
       StringBuilder sb = new StringBuilder();
       return sb.append("Failed to load resources.")
       .append(System.lineSeparator())
       .append(super.getMessage())
       .toString();
   }
}
