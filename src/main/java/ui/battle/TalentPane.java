package ui.battle;

import javafx.beans.binding.StringExpression;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import model.member.ExtendedBattleMember;
import model.member.generation.PrimaryAttribute;
import model.member.generation.Talent;
import ui.part.NumberField;

import java.util.Arrays;

public class TalentPane extends HBox {

    public TalentPane(ExtendedBattleMember battleMember, Talent talent) {
        this.setPadding(new Insets(5));
        this.setAlignment(Pos.CENTER);

        Label name = new Label(talent.toString());
        name.setPrefWidth(150);
        this.getChildren().add(name);

        Label attributes = new Label();
        attributes.textProperty().bind(
                Arrays.stream(talent.getAttributes()).map(PrimaryAttribute::toShortStringProperty)
                        .map(r -> (StringExpression) r)
                        .reduce((a, b) -> a.concat("/").concat(b))
                        .orElse(new ReadOnlyStringWrapper("--/--/--")));
        attributes.setPrefWidth(80);
        this.getChildren().add(attributes);

        NumberField points = new NumberField();
        points.setPrefWidth(30);

        points.numberProperty().bindBidirectional(battleMember.getTalent(talent));
        this.getChildren().add(points);
    }
}
