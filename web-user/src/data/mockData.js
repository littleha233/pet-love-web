export const featuredPets = [
  {
    id: 1,
    name: "米粒",
    city: "上海",
    type: "英短",
    age: "2 岁",
    status: "AVAILABLE",
    description: "亲人安静，已绝育，适合新手家庭。",
    tags: ["已免疫", "可回访", "同城优先"],
    imageUrl:
      "https://images.pexels.com/photos/1056251/pexels-photo-1056251.jpeg?auto=compress&cs=tinysrgb&w=1200"
  },
  {
    id: 2,
    name: "豆包",
    city: "杭州",
    type: "中华田园犬",
    age: "1.5 岁",
    status: "VERIFIED",
    description: "活泼但不拆家，熟悉牵引，适合有陪伴时间的家庭。",
    tags: ["健康体检", "平台审核", "可视频回访"],
    imageUrl:
      "https://images.pexels.com/photos/551628/pexels-photo-551628.jpeg?auto=compress&cs=tinysrgb&w=1200"
  },
  {
    id: 3,
    name: "小云",
    city: "南京",
    type: "橘猫",
    age: "3 岁",
    status: "ADOPTED",
    description: "性格温和，已找到稳定家庭，感谢每一位关注者。",
    tags: ["已领养", "回访中", "公益送养"],
    imageUrl:
      "https://images.pexels.com/photos/127028/pexels-photo-127028.jpeg?auto=compress&cs=tinysrgb&w=1200"
  }
];

export const adoptionPets = [
  ...featuredPets,
  {
    id: 4,
    name: "奶糖",
    city: "成都",
    type: "布偶",
    age: "8 个月",
    status: "AVAILABLE",
    description: "已做基础驱虫，社交友好，建议室内科学喂养。",
    tags: ["幼猫", "亲人", "站内留言"],
    imageUrl:
      "https://images.pexels.com/photos/1741235/pexels-photo-1741235.jpeg?auto=compress&cs=tinysrgb&w=1200"
  },
  {
    id: 5,
    name: "阿七",
    city: "苏州",
    type: "柯基",
    age: "4 岁",
    status: "VERIFIED",
    description: "稳定温顺，已完成年度免疫，适合有遛犬条件家庭。",
    tags: ["已免疫", "需家访", "同城优先"],
    imageUrl:
      "https://images.pexels.com/photos/733416/pexels-photo-733416.jpeg?auto=compress&cs=tinysrgb&w=1200"
  },
  {
    id: 6,
    name: "团子",
    city: "深圳",
    type: "奶牛猫",
    age: "2 岁",
    status: "AVAILABLE",
    description: "怕生但稳定，建议有养猫经验的家庭领养。",
    tags: ["需耐心", "可回访", "健康档案齐全"],
    imageUrl:
      "https://images.pexels.com/photos/730896/pexels-photo-730896.jpeg?auto=compress&cs=tinysrgb&w=1200"
  }
];

export const serviceCards = [
  {
    title: "标准上门喂养",
    description: "适合短期出差或节假日，流程透明可追踪。",
    items: ["到达打卡", "喂食换水", "猫砂清理", "离开打卡 + 图文留痕"],
    price: "¥68 / 次起"
  },
  {
    title: "细致照护服务",
    description: "增加互动陪伴和健康观察，适合敏感宠物。",
    items: ["标准喂养全部内容", "15 分钟互动", "异常行为记录", "即时提醒"],
    price: "¥98 / 次起"
  },
  {
    title: "多宠家庭方案",
    description: "支持多宠同访，价格透明，减少频繁沟通成本。",
    items: ["多宠喂养", "分宠照片", "补充消耗品提醒", "行程协同"],
    price: "¥128 / 次起"
  }
];

export const trustPoints = [
  {
    title: "实名认证与资质审核",
    text: "服务者需通过实名和基础资质审核后才可接单。"
  },
  {
    title: "全程留痕",
    text: "每次服务包含到达、服务中、离开三段记录。"
  },
  {
    title: "异常与投诉机制",
    text: "异常场景支持一键上报，客服介入并留存处理记录。"
  }
];

export const timelineSample = [
  { time: "09:02", content: "到达打卡，上传门口照片。" },
  { time: "09:08", content: "完成喂食和换水，上传宠物进食视频。" },
  { time: "09:15", content: "清理猫砂并补充记录，备注食欲正常。" },
  { time: "09:20", content: "离开打卡，用户可在订单页查看完整时间线。" }
];

export const homeFaq = [
  {
    q: "平台会做宠物医疗诊断吗？",
    a: "不会。平台提供救助指引和资源信息，不提供诊断和开药建议。"
  },
  {
    q: "如何确保领养信息真实？",
    a: "领养与送养信息需经过基础审核，支持举报与人工复核。"
  },
  {
    q: "上门喂养发生争议怎么办？",
    a: "可在订单页提交投诉，客服会基于留痕与沟通记录介入处理。"
  }
];

export const serviceFaq = [
  {
    q: "是否支持线下支付？",
    a: "MVP 首版支持线下支付，平台负责服务记录与流程留痕。"
  },
  {
    q: "服务者拒单后会怎样？",
    a: "订单会回到待匹配状态，你可继续选择其他同城服务者。"
  },
  {
    q: "是否必须授权精确定位？",
    a: "首版只记录基础时间与必要打卡信息，不强制精确轨迹采集。"
  }
];

export const processSteps = [
  "发布喂养需求：时间、地址、宠物习性",
  "选择服务者并发起预约",
  "服务者接单后按节点打卡与留痕",
  "完成服务后用户确认并评价"
];
