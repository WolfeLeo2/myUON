package com.wolfeleo2.myuon.data.db

import androidx.room.TypeConverter
import com.wolfeleo2.myuon.data.model.ClassType
import com.wolfeleo2.myuon.data.model.GenderTarget
import com.wolfeleo2.myuon.data.model.UnitStatus

class Converters {
    @TypeConverter
    fun fromUnitStatus(value: UnitStatus): String = value.name

    @TypeConverter
    fun toUnitStatus(value: String): UnitStatus = runCatching { UnitStatus.valueOf(value) }.getOrDefault(UnitStatus.AVAILABLE)

    @TypeConverter
    fun fromClassType(value: ClassType): String = value.name

    @TypeConverter
    fun toClassType(value: String): ClassType = runCatching { ClassType.valueOf(value) }.getOrDefault(ClassType.LECTURE)

    @TypeConverter
    fun fromGenderTarget(value: GenderTarget): String = value.name

    @TypeConverter
    fun toGenderTarget(value: String): GenderTarget = runCatching { GenderTarget.valueOf(value) }.getOrDefault(GenderTarget.CO_ED)
}
