import java.security.SecureRandom;

/**
 *
 * @author Stephen Adams
 */
public class Die {
   private final int faces;
   private SecureRandom rand;
   private int currentFace;
   
   public Die( int faces ) {
       this.faces = faces;
       rand = new SecureRandom();
       currentFace = roll();
   }

   public Die() { this( 6 );   }
   
   public int getCurrentFace() { return currentFace; }
   
   public int roll() { 
       currentFace = rand.nextInt( faces ) + 1;
       return currentFace;
   }
}
