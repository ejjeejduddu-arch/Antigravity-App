package com.example.gemini.ui.membership

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.launch

@Composable
fun MembershipPlansDialog(
    currentPlan: String,
    onDismiss: () -> Unit,
    onPlanUpdated: (String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var selectedProCredits by remember { mutableStateOf(100) }
    var activeTier by remember { mutableStateOf(currentPlan.lowercase()) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.92f),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                // Top Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Upgrade your plan",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Current: ${activeTier.replaceFirstChar { it.uppercase() }}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "Close",
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Scrollable Plan Cards List
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 1. PLUS (LIMITED TIME OFFER - FEATURED)
                    PlanCard(
                        title = "Plus",
                        badge = "LIMITED TIME",
                        headline = "Advanced capabilities",
                        description = "Personalized AI for life and work.",
                        strikePrice = "₹1,999",
                        price = "₹0",
                        period = "/ month",
                        buttonText = if (activeTier == "plus") "Current Plan" else "✨ Claim free offer",
                        isCurrentPlan = activeTier == "plus",
                        isHighlighted = true,
                        buttonColor = Color(0xFF2563EB),
                        buttonTextColor = Color.White,
                        features = listOf(
                            PlanFeature(Icons.Outlined.Star, "Advanced models for complex tasks (Claude 3.7, DeepSeek R1, Gemini Pro)"),
                            PlanFeature(Icons.Outlined.Image, "Higher quality image creation"),
                            PlanFeature(Icons.Outlined.WorkOutline, "Agentic actions across apps and files"),
                            PlanFeature(Icons.Outlined.Code, "Codex to automate coding"),
                            PlanFeature(Icons.Outlined.Psychology, "Expanded memory and 20 GB of storage"),
                            PlanFeature(Icons.Outlined.Link, "Email and calendar connections"),
                            PlanFeature(Icons.Outlined.Folder, "Larger projects to organize your work"),
                            PlanFeature(Icons.Outlined.QueryStats, "Tools for personal finance and data analysis"),
                            PlanFeature(Icons.Outlined.Check, "No ads")
                        ),
                        footerNote = "Promo terms apply. Promo pricing applies for 1 month. Starting Dec 7, 2026, ChatGPT / Antigravity Plus will continue at ₹1,999/month. Cancel anytime.",
                        onAction = {
                            activeTier = "plus"
                            onPlanUpdated("plus")
                            Toast.makeText(context, "🎉 Plus Plan activated! Claimed 1-Month Free Offer.", Toast.LENGTH_LONG).show()
                            onDismiss()
                        }
                    )

                    // 2. PRO (FRONTIER INTELLIGENCE)
                    PlanCard(
                        title = "Pro",
                        badge = null,
                        headline = "Frontier intelligence",
                        description = "Our most powerful models and agents.",
                        price = when (selectedProCredits) {
                            200 -> "₹19,999"
                            500 -> "₹44,999"
                            else -> "₹10,699"
                        },
                        period = "/ month (inclusive of GST)",
                        buttonText = if (activeTier == "pro") "Current Plan" else "Upgrade to Pro",
                        isCurrentPlan = activeTier == "pro",
                        isHighlighted = false,
                        buttonColor = MaterialTheme.colorScheme.onSurface,
                        buttonTextColor = MaterialTheme.colorScheme.surface,
                        creditsToggle = {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(99.dp))
                                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                                    .padding(2.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                listOf(100, 200, 500).forEach { credits ->
                                    val isSelected = selectedProCredits == credits
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(99.dp))
                                            .background(if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent)
                                            .clickable { selectedProCredits = credits }
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "$credits",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                            }
                        },
                        features = listOf(
                            PlanFeature(Icons.Outlined.AutoAwesome, "Our most capable frontier Pro model"),
                            PlanFeature(Icons.Outlined.AllInclusive, "Dots, your always-on agents"),
                            PlanFeature(Icons.Outlined.Terminal, "More Codex and agentic usage"),
                            PlanFeature(Icons.Outlined.Psychology, "Maximum memory and 100 GB of storage"),
                            PlanFeature(Icons.Outlined.Upcoming, "Early access to new tools and models"),
                            PlanFeature(Icons.Outlined.Check, "No ads"),
                            PlanFeature(Icons.Outlined.Security, "Unlimited subject to abuse guardrails")
                        ),
                        footerNote = "No commitment. Cancel anytime. Unlimited subject to abuse guardrails.",
                        onAction = {
                            activeTier = "pro"
                            onPlanUpdated("pro")
                            Toast.makeText(context, "🚀 Upgraded to Pro Plan ($selectedProCredits credits)!", Toast.LENGTH_LONG).show()
                            onDismiss()
                        }
                    )

                    // 3. GO (EXPANDED ACCESS)
                    PlanCard(
                        title = "Go",
                        badge = null,
                        headline = "Expanded access",
                        description = "Chat more with extended usage.",
                        price = "₹399",
                        period = "/ month (inclusive of GST)",
                        buttonText = if (activeTier == "go") "Current Plan" else "Upgrade to Go",
                        isCurrentPlan = activeTier == "go",
                        isHighlighted = false,
                        buttonColor = MaterialTheme.colorScheme.onSurface,
                        buttonTextColor = MaterialTheme.colorScheme.surface,
                        features = listOf(
                            PlanFeature(Icons.Outlined.ChatBubbleOutline, "More messages with tools"),
                            PlanFeature(Icons.Outlined.Image, "More image creation"),
                            PlanFeature(Icons.Outlined.Psychology, "More memory and storage"),
                            PlanFeature(Icons.Outlined.Mic, "More voice chats"),
                            PlanFeature(Icons.Outlined.Campaign, "Ad supported")
                        ),
                        footerNote = "No commitment. Cancel anytime. This plan may include ads.",
                        onAction = {
                            activeTier = "go"
                            onPlanUpdated("go")
                            Toast.makeText(context, "⚡ Upgraded to Go Plan!", Toast.LENGTH_LONG).show()
                            onDismiss()
                        }
                    )

                    // 4. FREE (TRY ANTIGRAVITY)
                    PlanCard(
                        title = "Free",
                        badge = null,
                        headline = "Try Antigravity",
                        description = "See what AI can do.",
                        price = "₹0",
                        period = "/ month",
                        buttonText = if (activeTier == "free") "Your current plan" else "Switch to Free",
                        isCurrentPlan = activeTier == "free",
                        isHighlighted = false,
                        buttonColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                        buttonTextColor = MaterialTheme.colorScheme.onSurface,
                        features = listOf(
                            PlanFeature(Icons.Outlined.ChatBubbleOutline, "Unlimited everyday text chats"),
                            PlanFeature(Icons.Outlined.Image, "Limited access to image creation"),
                            PlanFeature(Icons.Outlined.Psychology, "Limited memory and storage"),
                            PlanFeature(Icons.Outlined.Mic, "Limited voice chats"),
                            PlanFeature(Icons.Outlined.Campaign, "Ad supported")
                        ),
                        footerNote = "Unlimited subject to abuse guardrails.",
                        onAction = {
                            activeTier = "free"
                            onPlanUpdated("free")
                            Toast.makeText(context, "Switched to Free Plan", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

data class PlanFeature(
    val icon: ImageVector,
    val text: String
)

@Composable
private fun PlanCard(
    title: String,
    badge: String?,
    headline: String,
    description: String,
    price: String,
    strikePrice: String? = null,
    period: String,
    buttonText: String,
    isCurrentPlan: Boolean,
    isHighlighted: Boolean,
    buttonColor: Color,
    buttonTextColor: Color,
    creditsToggle: (@Composable () -> Unit)? = null,
    features: List<PlanFeature>,
    footerNote: String? = null,
    onAction: () -> Unit
) {
    val borderColor = if (isHighlighted) Color(0xFF3B82F6) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
    val cardBackground = if (isHighlighted) {
        Color(0xFF1E3A8A).copy(alpha = 0.08f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = cardBackground,
        border = androidx.compose.foundation.BorderStroke(if (isHighlighted) 2.dp else 1.dp, borderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            // Header Row (Name + Badge or Credits Toggle)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (badge != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(99.dp))
                            .background(Color(0xFF2563EB).copy(alpha = 0.15f))
                            .border(1.dp, Color(0xFF2563EB).copy(alpha = 0.4f), RoundedCornerShape(99.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = badge,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF60A5FA),
                            letterSpacing = 0.5.sp
                        )
                    }
                } else if (creditsToggle != null) {
                    creditsToggle()
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = headline,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = description,
                fontSize = 12.5.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
            )

            // Primary Action Button
            Button(
                onClick = onAction,
                enabled = !isCurrentPlan,
                shape = RoundedCornerShape(99.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = buttonColor,
                    contentColor = buttonTextColor,
                    disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                    disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
            ) {
                Text(
                    text = buttonText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Text(
                text = "No commitment. Cancel anytime.",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Pricing Row
            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp)
            ) {
                if (strikePrice != null) {
                    Text(
                        text = strikePrice,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        textDecoration = TextDecoration.LineThrough,
                        modifier = Modifier.padding(end = 6.dp)
                    )
                }

                Text(
                    text = price,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isHighlighted) Color(0xFF60A5FA) else MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = " $period",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                thickness = 1.dp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Feature Checklist
            Column(
                verticalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                features.forEach { feature ->
                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = feature.icon,
                            contentDescription = null,
                            modifier = Modifier
                                .size(16.dp)
                                .padding(top = 2.dp),
                            tint = if (isHighlighted) Color(0xFF60A5FA) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Text(
                            text = feature.text,
                            fontSize = 12.5.sp,
                            lineHeight = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                        )
                    }
                }
            }

            if (footerNote != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = footerNote,
                    fontSize = 10.5.sp,
                    lineHeight = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                )
            }
        }
    }
}
