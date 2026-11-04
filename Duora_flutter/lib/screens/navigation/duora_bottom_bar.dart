import 'package:flutter/material.dart';

enum DuoraTab {
  home,
  chat,
  call,
  notes,
  settings,
}

class DuoraBottomBar extends StatelessWidget {
  final DuoraTab currentTab;
  final int unreadChatCount;
  final ValueChanged<DuoraTab> onTabSelected;

  const DuoraBottomBar({
    super.key,
    required this.currentTab,
    this.unreadChatCount = 3,
    required this.onTabSelected,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      color: Colors.transparent,
      child: Stack(
        alignment: Alignment.bottomCenter,
        clipBehavior: Clip.none,
        children: [
          // 1. Curved Glassy Navigation Bar Background
          Container(
            height: 72,
            margin: const EdgeInsets.only(left: 16, right: 16, bottom: 18),
            decoration: BoxDecoration(
              gradient: const LinearGradient(
                colors: [
                  Color(0xFF2C195E),
                  Color(0xFF3F207D),
                  Color(0xFF211452),
                ],
                begin: Alignment.topLeft,
                end: Alignment.bottomRight,
              ),
              borderRadius: BorderRadius.circular(36),
              border: Border.all(
                color: const Color(0xFFB057F7).withOpacity(0.35),
                width: 1.5,
              ),
              boxShadow: [
                BoxShadow(
                  color: const Color(0xFF5B3DF5).withOpacity(0.35),
                  blurRadius: 24,
                  offset: const Offset(0, 8),
                ),
                BoxShadow(
                  color: Colors.black.withOpacity(0.4),
                  blurRadius: 16,
                  offset: const Offset(0, 4),
                ),
              ],
            ),
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceAround,
              children: [
                // 1. Home
                _NavItem(
                  label: 'Home',
                  icon: Icons.home_rounded,
                  isSelected: currentTab == DuoraTab.home,
                  onTap: () => onTabSelected(DuoraTab.home),
                ),

                // 2. Chat
                _NavItem(
                  label: 'Chat',
                  icon: Icons.chat_bubble_outline_rounded,
                  badgeCount: unreadChatCount,
                  isSelected: currentTab == DuoraTab.chat,
                  onTap: () => onNavigateTabOrScreen(DuoraTab.chat),
                ),

                // Placeholder space for center elevated Calls button
                const SizedBox(width: 58),

                // 4. Notes
                _NavItem(
                  label: 'Notes',
                  icon: Icons.sticky_note_2_outlined,
                  isSelected: currentTab == DuoraTab.notes,
                  onTap: () => onNavigateTabOrScreen(DuoraTab.notes),
                ),

                // 5. Settings
                _NavItem(
                  label: 'Settings',
                  icon: Icons.settings_rounded,
                  isSelected: currentTab == DuoraTab.settings,
                  onTap: () => onNavigateTabOrScreen(DuoraTab.settings),
                ),
              ],
            ),
          ),

          // 2. Floating Heart Accent above Centerpiece
          Positioned(
            bottom: 74,
            child: Container(
              padding: const EdgeInsets.all(4),
              decoration: BoxDecoration(
                shape: BoxShape.circle,
                boxShadow: [
                  BoxShadow(
                    color: const Color(0xFFFF2D8D).withOpacity(0.6),
                    blurRadius: 12,
                    spreadRadius: 2,
                  ),
                ],
              ),
              child: const Icon(
                Icons.favorite_rounded,
                color: Color(0xFFFFB6D5),
                size: 16,
              ),
            ),
          ),

          // 3. Signature Centerpiece: Elevated Floating Calls Button
          Positioned(
            bottom: 28,
            child: GestureDetector(
              onTap: () => onTabSelected(DuoraTab.call),
              child: AnimatedContainer(
                duration: const Duration(milliseconds: 250),
                width: 60,
                height: 60,
                decoration: BoxDecoration(
                  gradient: const LinearGradient(
                    colors: [
                      Color(0xFF5B3DF5),
                      Color(0xFFFF2D8D),
                    ],
                    begin: Alignment.topLeft,
                    end: Alignment.bottomRight,
                  ),
                  shape: BoxShape.circle,
                  border: Border.all(color: Colors.white, width: 2.5),
                  boxShadow: [
                    BoxShadow(
                      color: const Color(0xFFFF2D8D).withOpacity(0.55),
                      blurRadius: 18,
                      spreadRadius: 2,
                      offset: const Offset(0, 4),
                    ),
                  ],
                ),
                child: const Center(
                  child: Icon(
                    Icons.call_rounded,
                    color: Colors.white,
                    size: 28,
                  ),
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }

  void onNavigateTabOrScreen(DuoraTab tab) {
    onTabSelected(tab);
  }
}

class _NavItem extends StatelessWidget {
  final String label;
  final IconData icon;
  final bool isSelected;
  final int badgeCount;
  final VoidCallback onTap;

  const _NavItem({
    required this.label,
    required this.icon,
    required this.isSelected,
    this.badgeCount = 0,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: onTap,
      behavior: HitTestBehavior.opaque,
      child: AnimatedContainer(
        duration: const Duration(milliseconds: 200),
        padding: isSelected
            ? const EdgeInsets.symmetric(horizontal: 14, vertical: 8)
            : const EdgeInsets.symmetric(horizontal: 10, vertical: 6),
        decoration: isSelected
            ? BoxDecoration(
                color: const Color(0xFF5B3DF5).withOpacity(0.5),
                borderRadius: BorderRadius.circular(20),
                border: Border.all(
                  color: const Color(0xFFB057F7).withOpacity(0.6),
                  width: 1,
                ),
                boxShadow: [
                  BoxShadow(
                    color: const Color(0xFF5B3DF5).withOpacity(0.3),
                    blurRadius: 8,
                  ),
                ],
              )
            : null,
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Stack(
              clipBehavior: Clip.none,
              children: [
                Icon(
                  icon,
                  size: isSelected ? 22 : 20,
                  color: isSelected ? Colors.white : const Color(0xFFB3ADCA),
                ),
                if (badgeCount > 0)
                  Positioned(
                    top: -4,
                    right: -8,
                    child: Container(
                      padding: const EdgeInsets.all(3.5),
                      decoration: const BoxDecoration(
                        color: Color(0xFFFF2D8D),
                        shape: BoxShape.circle,
                      ),
                      child: Text(
                        badgeCount.toString(),
                        style: const TextStyle(
                          color: Colors.white,
                          fontSize: 8.5,
                          fontWeight: FontWeight.bold,
                        ),
                      ),
                    ),
                  ),
              ],
            ),
            const SizedBox(height: 3),
            Text(
              label,
              style: TextStyle(
                fontSize: 10,
                fontWeight: isSelected ? FontWeight.w700 : FontWeight.w500,
                color: isSelected ? Colors.white : const Color(0xFFB3ADCA),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
