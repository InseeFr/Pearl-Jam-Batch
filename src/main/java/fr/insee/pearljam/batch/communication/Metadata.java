package fr.insee.pearljam.batch.communication;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Getter
@Setter
@ToString
public class Metadata {
    private String key;
    private String value;
}
