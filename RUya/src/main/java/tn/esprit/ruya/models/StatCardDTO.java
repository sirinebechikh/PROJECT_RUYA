package tn.esprit.ruya.models;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StatCardDTO {
    private String number;
    private String label;
    private String amount;
    private String status;

    public StatCardDTO(String number, String label, String amount) {
        this.number = number;
        this.label = label;
        this.amount = amount;
    }
}