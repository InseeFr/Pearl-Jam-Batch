package fr.insee.pearljam.batch.communication;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class Metadata {
    private String key;
    private String value;
}
