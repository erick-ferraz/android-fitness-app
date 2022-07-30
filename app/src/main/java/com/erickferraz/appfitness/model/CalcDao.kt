package com.erickferraz.appfitness.model

import androidx.room.*

@Dao
interface CalcDao {

    @Insert fun insert(calc: Calc)
    @Query("SELECT * FROM Calc WHERE type = :type") fun getRegisterByType(type: String) : List<Calc>
    @Update fun update(calc: Calc)
    @Delete fun delete(calc: Calc)
}