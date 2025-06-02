package org.example.citrixcontrolrest.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DgLoadDTO {
    private String name;
    private String averageLoadIndex;
}

