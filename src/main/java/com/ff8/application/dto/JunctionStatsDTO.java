package com.ff8.application.dto;

/**
 * Data Transfer Object for junction stat bonuses.
 * Uses Java 21 record for immutability.
 */
public record JunctionStatsDTO(
        int hp,
        int str,
        int vit,
        int mag,
        int spr,
        int spd,
        int eva,
        int hit,
        int luck
) {
    /**
     * Validation constructor
     */
    public JunctionStatsDTO {
        if (hp < 0 || hp > 255) throw new IllegalArgumentException("HP must be 0-255");
        if (str < 0 || str > 255) throw new IllegalArgumentException("STR must be 0-255");
        if (vit < 0 || vit > 255) throw new IllegalArgumentException("VIT must be 0-255");
        if (mag < 0 || mag > 255) throw new IllegalArgumentException("MAG must be 0-255");
        if (spr < 0 || spr > 255) throw new IllegalArgumentException("SPR must be 0-255");
        if (spd < 0 || spd > 255) throw new IllegalArgumentException("SPD must be 0-255");
        if (eva < 0 || eva > 255) throw new IllegalArgumentException("EVA must be 0-255");
        if (hit < 0 || hit > 255) throw new IllegalArgumentException("HIT must be 0-255");
        if (luck < 0 || luck > 255) throw new IllegalArgumentException("LUCK must be 0-255");
    }

    /**
     * Create empty junction stats
     */
    public static JunctionStatsDTO empty() {
        return new JunctionStatsDTO(0, 0, 0, 0, 0, 0, 0, 0, 0);
    }

    /**
     * Check if any stats have bonuses
     */
    public boolean hasAnyBonuses() {
        return hp > 0 || str > 0 || vit > 0 || mag > 0 || 
               spr > 0 || spd > 0 || eva > 0 || hit > 0 || luck > 0;
    }

    /**
     * Get total stat bonus points
     */
    public int getTotalBonuses() {
        return hp + str + vit + mag + spr + spd + eva + hit + luck;
    }

    /**
     * Create a copy with modified HP
     */
    public JunctionStatsDTO withHp(int newHp) {
        return new JunctionStatsDTO(newHp, str, vit, mag, spr, spd, eva, hit, luck);
    }

    /**
     * Create a copy with modified STR
     */
    public JunctionStatsDTO withStr(int newStr) {
        return new JunctionStatsDTO(hp, newStr, vit, mag, spr, spd, eva, hit, luck);
    }

    /**
     * Create a copy with modified VIT
     */
    public JunctionStatsDTO withVit(int newVit) {
        return new JunctionStatsDTO(hp, str, newVit, mag, spr, spd, eva, hit, luck);
    }

    /**
     * Create a copy with modified MAG
     */
    public JunctionStatsDTO withMag(int newMag) {
        return new JunctionStatsDTO(hp, str, vit, newMag, spr, spd, eva, hit, luck);
    }

    /**
     * Create a copy with modified SPR
     */
    public JunctionStatsDTO withSpr(int newSpr) {
        return new JunctionStatsDTO(hp, str, vit, mag, newSpr, spd, eva, hit, luck);
    }

    /**
     * Create a copy with modified SPD
     */
    public JunctionStatsDTO withSpd(int newSpd) {
        return new JunctionStatsDTO(hp, str, vit, mag, spr, newSpd, eva, hit, luck);
    }

    /**
     * Create a copy with modified EVA
     */
    public JunctionStatsDTO withEva(int newEva) {
        return new JunctionStatsDTO(hp, str, vit, mag, spr, spd, newEva, hit, luck);
    }

    /**
     * Create a copy with modified HIT
     */
    public JunctionStatsDTO withHit(int newHit) {
        return new JunctionStatsDTO(hp, str, vit, mag, spr, spd, eva, newHit, luck);
    }

    /**
     * Create a copy with modified LUCK
     */
    public JunctionStatsDTO withLuck(int newLuck) {
        return new JunctionStatsDTO(hp, str, vit, mag, spr, spd, eva, hit, newLuck);
    }

    /**
     * Get formatted display string
     */
    public String getDisplayString() {
        if (!hasAnyBonuses()) {
            return "No stat bonuses";
        }
        
        var sb = new StringBuilder();
        if (hp > 0) sb.append("HP+").append(hp).append(" ");
        if (str > 0) sb.append("STR+").append(str).append(" ");
        if (vit > 0) sb.append("VIT+").append(vit).append(" ");
        if (mag > 0) sb.append("MAG+").append(mag).append(" ");
        if (spr > 0) sb.append("SPR+").append(spr).append(" ");
        if (spd > 0) sb.append("SPD+").append(spd).append(" ");
        if (eva > 0) sb.append("EVA+").append(eva).append(" ");
        if (hit > 0) sb.append("HIT+").append(hit).append(" ");
        if (luck > 0) sb.append("LUCK+").append(luck).append(" ");
        
        return sb.toString().trim();
    }
} 