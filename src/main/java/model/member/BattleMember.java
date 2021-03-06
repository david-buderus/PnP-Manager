package model.member;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import manager.LanguageUtility;
import manager.Utility;
import model.Battle;
import model.loot.LootTable;
import model.member.data.ArmorPiece;
import model.member.data.AttackTypes;
import model.member.state.interfaces.*;

import java.util.HashMap;

public class BattleMember extends Member {

    protected IntegerProperty life;
    protected IntegerProperty maxLife;
    protected IntegerProperty mana;
    protected IntegerProperty maxMana;
    protected IntegerProperty initiative;
    protected IntegerProperty startValue;
    protected IntegerProperty counter;
    protected IntegerProperty turns;
    protected IntegerProperty baseDefense;
    protected IntegerProperty level;

    protected LootTable lootTable;
    protected HashMap<ArmorPiece, IntegerProperty> armor;

    protected Battle battle;

    private final ListProperty<IMemberState> states;

    /**
     * Creates a BattleMember with default stats
     * and an empty LootTable
     *
     * @param battle the BattleMember is part of
     */
    public BattleMember(Battle battle) {
        this(battle, new LootTable());
    }

    /**
     * Copies values of the armor map into the own property.
     * These properties won't be linked!
     *
     * @param battle    the BattleMember is part of
     * @param lootTable of the given BattleMember
     * @param armor     which will be used as Armor
     */
    public BattleMember(Battle battle, LootTable lootTable, HashMap<ArmorPiece, IntegerProperty> armor) {
        this(battle, lootTable);
        for (ArmorPiece piece : armor.keySet()) {
            this.armor.get(piece).set(armor.get(piece).get());
        }
    }

    /**
     * Creates a BattleMember with default stats
     *
     * @param battle    the BattleMember is part of
     * @param lootTable of the given BattleMember
     */
    public BattleMember(Battle battle, LootTable lootTable) {
        super();
        this.name.set(LanguageUtility.getMessage("battleMember.defaultName"));
        this.life = new SimpleIntegerProperty(1);
        this.maxLife = new SimpleIntegerProperty(1);
        this.mana = new SimpleIntegerProperty(1);
        this.maxMana = new SimpleIntegerProperty(1);
        this.initiative = new SimpleIntegerProperty(1);
        this.startValue = new SimpleIntegerProperty(Utility.getConfig().getInt("character.initiative.start"));
        this.counter = new SimpleIntegerProperty(startValue.get());
        this.turns = new SimpleIntegerProperty(1);
        this.states = new SimpleListProperty<>(FXCollections.observableArrayList());
        this.baseDefense = new SimpleIntegerProperty(0);
        this.level = new SimpleIntegerProperty(1);

        this.life.addListener((ob, o, n) -> {
            if (isDead()) {
                this.turns.set(0);
            }
        });

        this.battle = battle;
        this.lootTable = lootTable;
        this.armor = new HashMap<>();

        for (ArmorPiece piece : ArmorPiece.values()) {
            this.armor.put(piece, new SimpleIntegerProperty(0));
        }

    }

    public void nextTurn() {
        if (isDead()) {
            return;
        }

        this.states.removeIf(state -> state.getDuration() < 1);


        this.turns.set(0);
        this.counter.set(getCounter() - calculateInitiative());

        while (getCounter() < 1) {
            turns.set(getTurns() + 1);
            this.counter.set(getCounter() + startValue.get());
        }

        for (IMemberState state : states) {
            if (state instanceof IManipulatingMemberState) {
                ((IManipulatingMemberState) state).apply(this);
            }
            state.decreaseDuration(false);
            state.decreaseDuration(true, getTurns());
        }
    }

    public void applyWearOnWeapons() { }

    private int calculateInitiative() {

        int init = getInitiative();

        for (IMemberState state : states) {
            if (state instanceof IAbsolutInitiativeMemberState) {
                init = ((IAbsolutInitiativeMemberState) state).apply(this, init);
            }
        }

        float relativeChange = 1;

        for (IMemberState state : states) {
            if (state instanceof IRelativeInitiativeMemberState) {
                relativeChange = ((IRelativeInitiativeMemberState) state).apply(this, relativeChange);
            }
        }

        init *= relativeChange;

        return Math.max(init, 0);
    }

    public void heal(int amount, BattleMember source) {
        this.life.set(Math.min(getLife() + amount, getMaxLife()));
        battle.addToHealStatistic(source, amount);
    }

    public void takeDamage(int amount, AttackTypes type, boolean withShield, double penetration, double block, BattleMember source) {

        for (IMemberState state : this.states) {
            if (state instanceof IIncomingDamageMemberState) {
                amount = ((IIncomingDamageMemberState) state).apply(this, amount);
            }
        }

        int damage = Math.max(0, amount - calculateDefense(type, withShield, penetration, block));
        this.life.set(getLife() - damage);
        battle.addToDamageStatistic(source, damage);
    }

    private int calculateDefense(AttackTypes type, boolean withShield, double penetration, double block) {
        int defense = 0;
        double reduction = 1 - penetration;

        switch (type) {
            case direct:
                return 0;
            case head:
                defense += getArmor(ArmorPiece.head);
                break;
            case upperBody:
                defense += getArmor(ArmorPiece.upperBody);
                break;
            case arm:
                defense += getArmor(ArmorPiece.arm);
                break;
            case legs:
                defense += getArmor(ArmorPiece.legs);
                break;
        }
        if (withShield) {
            defense += getArmor(ArmorPiece.shield) * block;
        }
        defense *= reduction;

        for (IMemberState state : this.states) {
            if (state instanceof IDefenseMemberState) {
                defense = ((IDefenseMemberState) state).apply(this, defense);
            }
        }

        return defense + baseDefense.get();
    }

    public void reset() {
        states.clear();
        this.counter.set(getStartValue());
        this.turns.set(1);
    }

    public void addState(IMemberState state) {
        this.states.add(state);
    }

    public void removeState(IMemberState state) {
        this.states.remove(state);
    }

    public LootTable getLootTable() {
        return lootTable;
    }

    public void setArmor(ArmorPiece target, int defense) {
        this.armor.get(target).unbind();
        this.armor.get(target).set(defense);
    }

    /**
     * Creates a new IntegerProperty and binds it
     * to the ObservableValue
     *
     * @param target  the specific ArmorPiece
     * @param defense ObservableValue which will get binded
     */
    protected void setArmor(ArmorPiece target, ObservableValue<Number> defense) {
        IntegerProperty property = new SimpleIntegerProperty(defense.getValue().intValue());
        property.bind(defense);
        this.armor.put(target, property);
    }

    public boolean isDead() {
        return getLife() <= 0;
    }

    protected int getArmor(ArmorPiece target) {
        return armor.get(target).get();
    }

    public BattleMember cloneMember() {
        BattleMember member = new BattleMember(battle, lootTable, armor);
        member.setDefense(baseDefense.get());
        member.setName(getName());
        member.setMaxLife(getMaxLife());
        member.setLife(getLife());
        member.setMaxMana(getMaxMana());
        member.setMana(getMana());
        member.setInitiative(getInitiative());
        member.startValue.set(getStartValue());
        member.counter.set(getCounter());
        member.turns.set(getTurns());

        return member;
    }

    public void setDefense(int defense) {
        this.baseDefense.set(defense);
    }

    public void setMaxLife(int life) {
        this.maxLife.set(life);
        this.life.set(life);
    }

    public void setMaxMana(int mana) {
        this.maxMana.set(mana);
        this.mana.set(mana);
    }

    public void setInitiative(int init) {
        this.initiative.set(init);
    }

    public int getLife() {
        return life.get();
    }

    public void setLife(int life) {
        this.life.set(life);
    }

    public IntegerProperty lifeProperty() {
        return life;
    }

    public int getMaxLife() {
        return maxLife.get();
    }

    public IntegerProperty maxLifeProperty() {
        return maxLife;
    }

    public int getMaxMana() {
        return maxMana.get();
    }

    public IntegerProperty maxManaProperty() {
        return maxMana;
    }

    public int getMana() {
        return mana.get();
    }

    public void setMana(int mana) {
        this.mana.set(Math.max(0, Math.min(mana, getMana())));
    }

    public IntegerProperty manaProperty() {
        return mana;
    }

    public int getInitiative() {
        return initiative.get();
    }

    public IntegerProperty initiativeProperty() {
        return initiative;
    }

    public int getStartValue() {
        return startValue.get();
    }

    public IntegerProperty startValueProperty() {
        return startValue;
    }

    public int getCounter() {
        return counter.get();
    }

    public IntegerProperty counterProperty() {
        return counter;
    }

    public int getTurns() {
        return turns.get();
    }

    public IntegerProperty turnsProperty() {
        return turns;
    }

    public ListProperty<IMemberState> statesProperty() {
        return states;
    }

    public IntegerProperty baseDefenseProperty() {
        return baseDefense;
    }

    public void setLevel(int level) {
        this.level.set(level);
    }

    public int getLevel() {
        return level.get();
    }

    public IntegerProperty levelProperty() {
        return level;
    }

    public int getTier() {
        return (int) Math.ceil(this.getLevel() / 5f);
    }

    public IntegerProperty armorProperty(ArmorPiece piece) {
        return armor.get(piece);
    }
}