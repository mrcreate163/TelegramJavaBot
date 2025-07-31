package prototype.javabot.model.aiSettings;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserAiSetting {

    @Builder.Default
    private ResponseLanguage language = ResponseLanguage.RUSSIAN;

    @Builder.Default
    private ContentLength length = ContentLength.MEDIUM;

    @Builder.Default
    private CommunicationStyle style = CommunicationStyle.FRIENDLY;

    public String getAllInstructions() {
        return String.join(" ",
                language.getInstruction(),
                length.getInstruction(),
                style.getInstruction());
    }

    public static UserAiSetting getDefault() {
        return UserAiSetting.builder().build();
    }

    public String getSettingsSummary() {
        return String.format("%s | %s | %s",
                language.getDisplayName(),
                length.getDisplayName(),
                style.getDisplayName());
    }

}
