package model.member.generation;

import model.item.Armor;
import model.item.Weapon;
import model.loot.LootTable;
import model.member.BattleMember;

import java.util.*;

public abstract class GenerationBase {

    protected Random random;
    protected String name;

    protected Collection<String> advantages;
    protected Collection<String> disadvantages;

    protected boolean dropsWeapon;
    protected boolean dropsArmor;
    protected boolean dropsJewellery;

    protected boolean ableToUsesPrimaryHand;
    protected boolean ableToUsesSecondaryHand;
    protected boolean ableToUseShield;
    protected Map<ArmorPosition, Boolean> ableToUseArmor;
    protected boolean ableToUseJewellery;
    protected boolean ableToUseSpells;
    protected boolean usesAlwaysShield;

    protected Collection<Talent> mainTalents;
    protected Collection<Talent> forbiddenTalents;
    protected Collection<GenerationBase> parents;
    protected Collection<PrimaryAttribute> primaryAttributes;
    protected Collection<SecondaryAttribute> secondaryAttributes;
    protected Collection<Drop> drops;
    protected Collection<String> primaryWeaponTypes;
    protected Collection<String> secondaryWeaponTypes;
    protected Collection<Weapon> specificPrimaryWeapons;
    protected Collection<Weapon> specificSecondaryWeapons;
    protected Map<ArmorPosition, Collection<Armor>> specificArmor;

    protected GenerationBase() {
        this.random = new Random();
        this.specificArmor = new HashMap<>();
        this.ableToUseArmor = new HashMap<>();
        this.advantages = new ArrayList<>();
        this.disadvantages = new ArrayList<>();
        this.mainTalents = new ArrayList<>();
        this.forbiddenTalents = new ArrayList<>();
        this.parents = new ArrayList<>();
        this.primaryAttributes = new ArrayList<>();
        this.secondaryAttributes = new ArrayList<>();
        this.drops = new ArrayList<>();
        this.primaryWeaponTypes = new ArrayList<>();
        this.secondaryWeaponTypes = new ArrayList<>();
        this.specificPrimaryWeapons = new ArrayList<>();
        this.specificSecondaryWeapons = new ArrayList<>();
    }

    public LootTable getLootTable(BattleMember member) {
        LootTable result = new LootTable();

        for (GenerationBase parent : parents) {
            result.add(parent.getLootTable(member));
        }

        for (Drop drop : drops) {
            drop.addToLootTable(result, member);
        }

        return result;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Collection<String> getAdvantages() {
        Collection<String> result = new ArrayList<>(advantages);
        for (GenerationBase parent : parents) {
            result.addAll(parent.getAdvantages());
        }
        return result;
    }

    public void setAdvantages(Collection<String> advantages) {
        this.advantages = advantages;
    }

    public Collection<String> getDisadvantages() {
        Collection<String> result = new ArrayList<>(disadvantages);
        for (GenerationBase parent : parents) {
            result.addAll(parent.getDisadvantages());
        }
        return result;
    }

    public void setDisadvantages(Collection<String> disadvantages) {
        this.disadvantages = disadvantages;
    }

    public boolean dropsWeapon() {
        return dropsWeapon && parents.stream().map(GenerationBase::dropsWeapon).reduce(true, (a, b) -> a && b);
    }

    public void setDropsWeapon(boolean dropsWeapon) {
        this.dropsWeapon = dropsWeapon;
    }

    public boolean dropsArmor() {
        return dropsArmor && parents.stream().map(GenerationBase::dropsArmor).reduce(true, (a, b) -> a && b);
    }

    public void setDropsArmor(boolean dropsArmor) {
        this.dropsArmor = dropsArmor;
    }

    public boolean dropsJewellery() {
        return dropsJewellery && parents.stream().map(GenerationBase::dropsJewellery).reduce(true, (a, b) -> a && b);
    }

    public void setDropsJewellery(boolean dropsJewellery) {
        this.dropsJewellery = dropsJewellery;
    }

    public boolean isAbleToUsesPrimaryHand() {
        return ableToUsesPrimaryHand && parents.stream().map(GenerationBase::isAbleToUsesPrimaryHand).reduce(true, (a, b) -> a && b);
    }

    public void setAbleToUsesPrimaryHand(boolean ableToUsesPrimaryHand) {
        this.ableToUsesPrimaryHand = ableToUsesPrimaryHand;
    }

    public boolean isAbleToUsesSecondaryHand() {
        return ableToUsesSecondaryHand && parents.stream().map(GenerationBase::isAbleToUsesSecondaryHand).reduce(true, (a, b) -> a && b);
    }

    public void setAbleToUsesSecondaryHand(boolean ableToUsesSecondaryHand) {
        this.ableToUsesSecondaryHand = ableToUsesSecondaryHand;
    }

    public boolean isAbleToUseShield() {
        return ableToUseShield && parents.stream().map(GenerationBase::isAbleToUseShield).reduce(true, (a, b) -> a && b);
    }

    public void setAbleToUseShield(boolean ableToUseShield) {
        this.ableToUseShield = ableToUseShield;
    }

    public boolean isAbleToUseArmor(ArmorPosition position) {
        return ableToUseArmor.getOrDefault(position, false) && parents.stream().map(g -> g.isAbleToUseArmor(position)).reduce(true, (a, b) -> a && b);
    }

    public void setAbleToUseArmor(ArmorPosition position, boolean ableToUseArmor) {
        this.ableToUseArmor.put(position, ableToUseArmor);
    }

    public boolean isAbleToUseJewellery() {
        return ableToUseJewellery && parents.stream().map(GenerationBase::isAbleToUseJewellery).reduce(true, (a, b) -> a && b);
    }

    public void setAbleToUseJewellery(boolean ableToUseJewellery) {
        this.ableToUseJewellery = ableToUseJewellery;
    }

    public boolean isAbleToUseSpells() {
        return ableToUseSpells && parents.stream().map(GenerationBase::isAbleToUseSpells).reduce(true, (a, b) -> a && b);
    }

    public void setAbleToUseSpells(boolean ableToUseSpells) {
        this.ableToUseSpells = ableToUseSpells;
    }

    public boolean usesAlwaysShield() {
        return usesAlwaysShield || parents.stream().map(GenerationBase::isAbleToUseSpells).reduce(false, (a, b) -> a || b);
    }

    public void setUsesAlwaysShield(boolean usesAlwaysShield) {
        this.usesAlwaysShield = usesAlwaysShield;
    }

    public Collection<? extends GenerationBase> getParents() {
        return parents;
    }

    public void addParent(GenerationBase parent) {
        this.parents.add(parent);
    }

    public Collection<Talent> getMainTalents() {
        Collection<Talent> result = new ArrayList<>(mainTalents);
        for (GenerationBase parent : parents) {
            result.addAll(parent.getMainTalents());
        }
        return result;
    }

    public void addMainTalent(Talent talent) {
        this.mainTalents.add(talent);
    }

    public Collection<Talent> getForbiddenTalents() {
        Collection<Talent> result = new ArrayList<>(forbiddenTalents);
        for (GenerationBase parent : parents) {
            result.addAll(parent.getForbiddenTalents());
        }
        return result;
    }

    public void addForbiddenTalent(Talent talent) {
        this.forbiddenTalents.add(talent);
    }

    public Collection<PrimaryAttribute> getPrimaryAttributes() {
        Collection<PrimaryAttribute> result = new ArrayList<>(primaryAttributes);
        for (GenerationBase parent : parents) {
            result.addAll(parent.getPrimaryAttributes());
        }
        return result;
    }

    public void setPrimaryAttributes(Collection<PrimaryAttribute> primaryAttributes) {
        this.primaryAttributes = primaryAttributes;
    }

    public Collection<SecondaryAttribute> getSecondaryAttributes() {
        Collection<SecondaryAttribute> result = new ArrayList<>(secondaryAttributes);
        for (GenerationBase parent : parents) {
            result.addAll(parent.getSecondaryAttributes());
        }
        return result;
    }

    public void setSecondaryAttributes(Collection<SecondaryAttribute> secondaryAttributes) {
        this.secondaryAttributes = secondaryAttributes;
    }

    public Collection<Drop> getDrops() {
        return drops;
    }

    public void setDrops(Collection<Drop> drops) {
        this.drops = drops;
    }

    public Collection<Weapon> getSpecificPrimaryWeapons() {
        Collection<Weapon> result = new ArrayList<>(specificPrimaryWeapons);
        for (GenerationBase parent : parents) {
            result.addAll(parent.getSpecificPrimaryWeapons());
        }
        return result;
    }

    public void setSpecificPrimaryWeapons(Collection<Weapon> specificPrimaryWeapons) {
        this.specificPrimaryWeapons = specificPrimaryWeapons;
    }

    public Collection<Weapon> getSpecificSecondaryWeapons() {
        Collection<Weapon> result = new ArrayList<>(specificSecondaryWeapons);
        for (GenerationBase parent : parents) {
            result.addAll(parent.getSpecificSecondaryWeapons());
        }
        return result;
    }

    public void setSpecificSecondaryWeapons(Collection<Weapon> specificSecondaryWeapons) {
        this.specificSecondaryWeapons = specificSecondaryWeapons;
    }

    public Collection<Armor> getSpecificArmor(ArmorPosition position) {
        Collection<Armor> result = new ArrayList<>(specificArmor.getOrDefault(position, Collections.emptyList()));
        for (GenerationBase parent : parents) {
            result.addAll(parent.getSpecificArmor(position));
        }
        return result;
    }

    public void setSpecificArmor(ArmorPosition position, Collection<Armor> armor) {
        this.specificArmor.put(position, armor);
    }

    public Collection<String> getPrimaryWeaponTypes() {
        Collection<String> result = new ArrayList<>(primaryWeaponTypes);
        for (GenerationBase parent : parents) {
            result.addAll(parent.getPrimaryWeaponTypes());
        }
        return result;
    }

    public void setPrimaryWeaponTypes(Collection<String> primaryWeaponTypes) {
        this.primaryWeaponTypes = primaryWeaponTypes;
    }

    public Collection<String> getSecondaryWeaponTypes() {
        Collection<String> result = new ArrayList<>(secondaryWeaponTypes);
        for (GenerationBase parent : parents) {
            result.addAll(parent.getSecondaryWeaponTypes());
        }
        return result;
    }

    public void setSecondaryWeaponTypes(Collection<String> secondaryWeaponTypes) {
        this.secondaryWeaponTypes = secondaryWeaponTypes;
    }

    @Override
    public String toString() {
        return name;
    }
}
