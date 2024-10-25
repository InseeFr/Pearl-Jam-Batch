package fr.insee.pearljam.batch.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class InterviewerDto {
	
	private String idep;
	private Long idSirh;
	private String nom;
	private String prenom;
	private String sexe;
	private String mailInsee;
	private String telInsee;
	private String telAutre;
	private String poleGestionCourant;

}
