class UserProfile {
  final String uid;
  final String email;
  final String username;
  final String fullName;
  final String partnerName;
  final String partnerUsername;
  final String myPairCode;
  final String partnerPairCode;
  final bool isPaired;
  final DateTime relationshipStartDate;
  final String statusMsg;
  final String? avatarUrl;
  final String? partnerAvatarUrl;
  final bool isVerified;
  final bool hasBlueTick;
  final bool hasGreenTick;
  final bool isOnline;

  UserProfile({
    required this.uid,
    required this.email,
    required this.username,
    required this.fullName,
    this.partnerName = 'My Partner',
    this.partnerUsername = '',
    required this.myPairCode,
    this.partnerPairCode = '',
    this.isPaired = false,
    DateTime? relationshipStartDate,
    this.statusMsg = 'Always together ❤️',
    this.avatarUrl,
    this.partnerAvatarUrl,
    this.isVerified = false,
    this.hasBlueTick = false,
    this.hasGreenTick = false,
    this.isOnline = true,
  }) : relationshipStartDate =
            relationshipStartDate ?? DateTime.now().subtract(const Duration(days: 365));

  Map<String, dynamic> toMap() {
    return {
      'uid': uid,
      'email': email,
      'username': username,
      'fullName': fullName,
      'partnerName': partnerName,
      'partnerUsername': partnerUsername,
      'myPairCode': myPairCode,
      'partnerPairCode': partnerPairCode,
      'isPaired': isPaired,
      'relationshipStartDate': relationshipStartDate.millisecondsSinceEpoch,
      'statusMsg': statusMsg,
      'avatarUrl': avatarUrl,
      'partnerAvatarUrl': partnerAvatarUrl,
      'isVerified': isVerified,
      'hasBlueTick': hasBlueTick,
      'hasGreenTick': hasGreenTick,
      'isOnline': isOnline,
    };
  }

  factory UserProfile.fromMap(Map<String, dynamic> map, String uid) {
    return UserProfile(
      uid: uid,
      email: map['email'] ?? '',
      username: map['username'] ?? 'duora_user',
      fullName: map['fullName'] ?? 'You',
      partnerName: map['partnerName'] ?? 'My Partner',
      partnerUsername: map['partnerUsername'] ?? '',
      myPairCode: map['myPairCode'] ?? '',
      partnerPairCode: map['partnerPairCode'] ?? '',
      isPaired: map['isPaired'] ?? false,
      relationshipStartDate: map['relationshipStartDate'] != null
          ? DateTime.fromMillisecondsSinceEpoch(map['relationshipStartDate'])
          : null,
      statusMsg: map['statusMsg'] ?? 'Always together ❤️',
      avatarUrl: map['avatarUrl'],
      partnerAvatarUrl: map['partnerAvatarUrl'],
      isVerified: map['isVerified'] ?? false,
      hasBlueTick: map['hasBlueTick'] ?? false,
      hasGreenTick: map['hasGreenTick'] ?? false,
      isOnline: map['isOnline'] ?? false,
    );
  }

  UserProfile copyWith({
    String? partnerName,
    String? partnerUsername,
    String? partnerPairCode,
    bool? isPaired,
    String? statusMsg,
    String? avatarUrl,
    String? partnerAvatarUrl,
    bool? isOnline,
  }) {
    return UserProfile(
      uid: uid,
      email: email,
      username: username,
      fullName: fullName,
      partnerName: partnerName ?? this.partnerName,
      partnerUsername: partnerUsername ?? this.partnerUsername,
      myPairCode: myPairCode,
      partnerPairCode: partnerPairCode ?? this.partnerPairCode,
      isPaired: isPaired ?? this.isPaired,
      relationshipStartDate: relationshipStartDate,
      statusMsg: statusMsg ?? this.statusMsg,
      avatarUrl: avatarUrl ?? this.avatarUrl,
      partnerAvatarUrl: partnerAvatarUrl ?? this.partnerAvatarUrl,
      isVerified: isVerified,
      hasBlueTick: hasBlueTick,
      hasGreenTick: hasGreenTick,
      isOnline: isOnline ?? this.isOnline,
    );
  }
}
