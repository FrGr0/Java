package trfMain;

/* 29/11/2018 modif FG : lecture du fichier de config en fonction du mode de trf demandé 
 * (inutile de stocker des valeurs vides dans les fichiers de conf) */

import java.io.*;
import java.util.Date;
import java.util.Calendar;
import java.util.Properties;
import java.text.SimpleDateFormat;
import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import jcifs.smb.*;

public class TrfMain {

	public static void main(String[] args) throws IOException 
	{
		/* controle du nombre d'arguments */		
		if (args.length<3) {
			System.out.println("parametres manquants...");
			System.out.println(" 1 - Fichier de config");
			System.out.println(" 2 - Fichier a transferer");
			System.out.println(" 2 - Mode de transfert (SMB, MAIL ou MIXTE)");
			System.out.println(" 3 - Repertoire SMB (optionnel)");
			System.exit(100);
		}
		
		/* recuperation des arguments du pg */
		String strCfgFile = args[0];
		String strFileIn  = args[1];
		String modeTrf    = args[2];
		String comppath   = "";
		
		/* affichage dbg */
		System.out.println( "********************" );
		System.out.println( "FICHIER DE CONF   : "+strCfgFile);
		System.out.println( "FICHIER A TRAITER : "+strFileIn);
		System.out.println( "MODE DE TRANSFERT : "+modeTrf);
		
		if (args.length==4) {			
			System.out.println( "REP SMB : "+args[3]);
			comppath = "/"+args[3];
		}		
		System.out.println( "********************" );
		
		//lecture du fichier de configuration
		Properties cfg            = new Properties();
		FileInputStream cfgStream = new FileInputStream(strCfgFile);
					
		File fileIn = new File(strFileIn); 
		
		/* variables lecture conf */
		String smbUrl    = "";
		String smbDir    = "";
		String smbPass   = "";
		String smbUser   = "";
		String toEmail   = "";
		String fromEmail = "";
		String objEmail  = "";
				
		if ( modeTrf.equals("SMB") || modeTrf.equals("MIXTE")) 
		{			
			cfg.load(cfgStream);		
			smbUser = cfg.getProperty("smb_user");
		    smbPass = cfg.getProperty("smb_pass");
		    smbDir  = cfg.getProperty("smb_share"); 
		    smbUrl  = cfg.getProperty("smb_url");
		    cfgStream.close();	
			
			String fileContent = getContent(fileIn);
			sendSMB( smbDir, comppath, fileIn.getName(), smbUser, smbPass, fileContent );	
			
		}
		
		if ( modeTrf.equals("MAIL") || modeTrf.equals("MIXTE") ) 
		{		
			cfg.load(cfgStream);	
		    toEmail   = cfg.getProperty("smtp_to");		
		    fromEmail = cfg.getProperty("smtp_from" );
			objEmail  = cfg.getProperty("smtp_obj");			
			cfgStream.close();	
			
			objEmail = String.format( objEmail, fileIn.getName(), now() );			
			String compMailBody="\r\n";
			if (modeTrf.equals("MIXTE")) {
				compMailBody+="fichier déposé sur "+smbUrl+"/"+comppath;
			}			
			String body = "Export automatique du fichier : "+fileIn.getName()+"\r\nle "+now()+compMailBody;
			
			Properties props = System.getProperties();
		    props.put("mail.smtp.host", "SMTP");
		    Session session = Session.getInstance(props, null);
		    
			sendEmail(session, fromEmail, toEmail, objEmail, body, strFileIn);
		}
		
		System.out.println( "*** Fin normale du programme ***" );
		System.exit(0);
	}

	private static void sendSMB( String sharedFolder, String comppath, 
								String FileName, String user, String pass,
								String fileContent)
	{
		try {
			
			String filePath="smb://"+sharedFolder+comppath+"/"+FileName;
			String fileDirectory ="smb://"+sharedFolder+comppath+"/";
		    
		    NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication("", user, pass);
		    		    
		    /* on essaye de créer les repertoires */
		    SmbFile smbDirectory = new SmbFile(fileDirectory, auth);
		    try {
		    	smbDirectory.mkdirs();
		    }
		    catch (Exception ex) {
		    	System.out.println( "erreur de creation du repertoire : "+ex.getMessage() );
		    }
		    
		    /* transfert du fichier */		    
		    SmbFile smbFile = new SmbFile(filePath, auth);
		    
		    SmbFileOutputStream smbfos = new SmbFileOutputStream(smbFile, false);	    
		    smbfos.write(fileContent.getBytes());
		    smbfos.close();
		    
		    System.out.println("*** transfert SMB : OK ! ***");
			
		} 
		catch (Exception e) {	         
           System.out.println(e.getMessage());
           e.printStackTrace();
      }
	}
	
	private static void sendEmail(Session session, String fromEmail, 
								 String toEmail, String subject, 
								 String body, String FileName)
	{		
		try
		{						
	         MimeMessage msg = new MimeMessage(session);
	         msg.addHeader("Content-type", "text/HTML; charset=UTF-8");
		     msg.addHeader("format", "flowed");
		     msg.addHeader("Content-Transfer-Encoding", "8bit");
		      
		     msg.setFrom(new InternetAddress(fromEmail, "automate"));
		     msg.setReplyTo(InternetAddress.parse(fromEmail, false));

		     msg.setSubject(subject, "UTF-8");

		     msg.setSentDate(new Date());
		     		    
		     InternetAddress[] iAdressArray = InternetAddress.parse(toEmail.replace(';',','));
		    		     
		     msg.setRecipients(Message.RecipientType.TO, iAdressArray);
		      	         
	         BodyPart messageBodyPart = new MimeBodyPart();

	         /* corps du message */
	         messageBodyPart.setText(body);
	         
	         /* multipart pour corps + pieces jointes */
	         Multipart multipart = new MimeMultipart();

	         /* Ajoute le corps du mail */
	         multipart.addBodyPart(messageBodyPart);

	         /* 2em partie : piece jointe */
	         messageBodyPart = new MimeBodyPart();
	         DataSource source = new FileDataSource(FileName);
	         messageBodyPart.setDataHandler(new DataHandler(source));
	         messageBodyPart.setFileName(FileName);
	         
	         /* ajoute la pj */
	         multipart.addBodyPart(messageBodyPart);

	         /* Prepare tous les composants du message */
	         msg.setContent(multipart);

	         /* Envoi du message */
	         Transport.send(msg);
	         
	         System.out.println("*** envoi mail : OK ! ***");
	         
	      } catch (Exception e) {	         
	         System.out.println(e.getMessage());
	         e.printStackTrace();
	      }
	}
	
	/* lecture du contenu du fichier a transferer*/
	private static String getContent (final File file)
	throws IOException {
	    StringBuilder result = new StringBuilder();
	    BufferedReader reader = new BufferedReader(new FileReader(file));	
	    try {
	        char[] buf = new char[1024];
	
	        int r = 0;
	
	        while ((r = reader.read(buf)) != -1) {
	            result.append(buf, 0, r);
	        }
	    }
	    finally {
	        reader.close();
	    }	
	    return result.toString();
	}
	
	private static String now() 
	{
		SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		Date today = Calendar.getInstance().getTime();        
		return df.format(today);
	}
}
	
