package fr.insee.pearljam.batch.config;

import org.springframework.stereotype.Component;

@Component
public class ApplicationConfig {

	public static String dbHost;
	public static String dbPort;
	public static String dbSchema;
	public static String dbUser;
	public static String dbPassword;
	public static String dbDriver;
	public static String pilotageDbHost;
	public static String pilotageDbPort;
	public static String pilotageDbSchema;
	public static String pilotageDbUser;
	public static String pilotageDbPassword;
	public static String pilotageDbDriver;
	public static String FOLDER_IN;
	public static String FOLDER_OUT;
	public static String contextReferentialScheme;
	public static String contextReferentialHost;
	public static String contextReferentialPort;
	public static String contextReferentialPath;
	public static String authServerURL;
	public static String realm;
	public static String FOLDER_IN_QUEEN;
	public static String FOLDER_OUT_QUEEN;
	public static String ldapServiceUrlScheme;
	public static String ldapServiceUrlHost;
	public static String ldapServiceUrlPort;
	public static String ldapServiceUrlPath;

	public ApplicationConfig() {
		super();
	}

}
