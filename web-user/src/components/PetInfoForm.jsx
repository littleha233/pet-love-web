function PetInfoForm({ form, onChange }) {
  return (
    <div className="form-grid-two">
      <label>
        宠物类型
        <select name="petType" value={form.petType} onChange={onChange} required>
          <option value="CAT">猫</option>
          <option value="DOG">狗</option>
        </select>
      </label>
      <label>
        宠物昵称
        <input name="petName" value={form.petName} onChange={onChange} maxLength={64} />
      </label>
      <label>
        性别
        <select name="petGender" value={form.petGender} onChange={onChange}>
          <option value="">未知</option>
          <option value="MALE">公</option>
          <option value="FEMALE">母</option>
          <option value="UNKNOWN">未知</option>
        </select>
      </label>
      <label>
        年龄（月）
        <input
          name="ageMonths"
          type="number"
          min={0}
          max={360}
          value={form.ageMonths}
          onChange={onChange}
        />
      </label>
      <label>
        品种
        <input name="breed" value={form.breed} onChange={onChange} maxLength={128} />
      </label>
      <label>
        体重（kg）
        <input name="weightKg" type="number" step="0.1" min={0} max={99.99} value={form.weightKg} onChange={onChange} />
      </label>
      <label>
        绝育状态
        <select name="neuteredStatus" value={form.neuteredStatus} onChange={onChange}>
          <option value="">未知</option>
          <option value="YES">已绝育</option>
          <option value="NO">未绝育</option>
          <option value="UNKNOWN">未知</option>
        </select>
      </label>
      <label>
        疫苗状态
        <select name="vaccinatedStatus" value={form.vaccinatedStatus} onChange={onChange}>
          <option value="">未知</option>
          <option value="YES">已完成</option>
          <option value="PARTIAL">部分完成</option>
          <option value="NO">未接种</option>
          <option value="UNKNOWN">未知</option>
        </select>
      </label>
      <label className="full-row">
        性格标签（逗号分隔，最多10个）
        <input
          name="temperamentTags"
          value={form.temperamentTags}
          onChange={onChange}
          placeholder="亲人, 活泼, 可摸"
        />
      </label>
      <label className="full-row">
        健康说明
        <textarea name="healthNote" value={form.healthNote} onChange={onChange} maxLength={1000} rows={3} />
      </label>
      <label className="full-row">
        特殊照护说明
        <textarea
          name="specialCareNote"
          value={form.specialCareNote}
          onChange={onChange}
          maxLength={1000}
          rows={3}
        />
      </label>
    </div>
  );
}

export default PetInfoForm;
