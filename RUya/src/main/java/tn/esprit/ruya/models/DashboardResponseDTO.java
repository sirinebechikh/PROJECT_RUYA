package tn.esprit.ruya.models;

import lombok.Data;
import java.util.List;

@Data
public class DashboardResponseDTO {
    private List<CardDataDTO> cardData;
    private List<StatCardDTO> globalStats;
}